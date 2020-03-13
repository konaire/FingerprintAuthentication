# Данный код и эта статья устарели, гугл выпустил свою собственную библиотеку для этих целей с [мануалом](https://developer.android.com/training/sign-in/biometric-auth).

# Авторизация через отпечатки пальцев на Android
Добрый день, в этой статье я расскажу и покажу как сделать простейший сканер отпечатков пальцев на андроиде, оформить его в соответствие с material design и с помощью него произвести авторизацию на сервере.

Давайте сразу же перейдем к ограничениям, которые накладывает ОС на этот функционал. За исключением Samsung'a данная "фича" стала появляться на устройствах только с релизом Android 6.0 (Marshmallow), в котором было добавлено API для работы с отпечатками пальцев. Поэтому если вы собираетесь сделать авторизацию только на основе отпечатков, вам нужно выставить `minSdkVersion 23` в файле **build.gradle**.

Это довольно существенное ограничение, на текущий момент всего лишь 56% процентов всех девайсов способны запустить такое приложение. Так что, если вы разрабатываете приложение для широкого круга пользователей и собираетесь выложить его в Google Play, возможно, вам стоит задуматься о реализации дополнительных способов авторизации.

[![Процентное соотношение разных версий Android](https://chart.googleapis.com/chart?chd=t:0.4,0.5,5.6,12.8,25.1,28.7,26.3,0.7&chf=bg,s,00000000&chl=Gingerbread|Ice%20Cream%20Sandwich|Jelly%20Bean|KitKat|Lollipop|Marshmallow|Nougat|Oreo&cht=p&chs=500x250&chco=c4df9b,6fad0c)](https://developer.android.com/about/dashboards/index.html)

_Статистика Google по распределению версий Android на устройствах (Январь 2018 г.)_

## Логика авторизации

Для упрощения задачи будем рассматривать только авторизацию через сканер отпечатков пальцев. Итак, что нужно для того, чтобы пользователь смог авторизоваться? В общем случае, для авторизации используется какой-то серверный запрос, в который передается логин и пароль, также, для того чтобы войти под каким-то логином, этот логин нужно предварительно создать, т. е. зарегистрироваться. В нашем простейшем случае давайте не будем реализовывать регистрацию, а договоримся, что сервер при первой попытке авторизации, запоминает переданную пару логин-пароль и использует ее для дальнейшей проверки.

Теперь мы можем написать простейшую реализацию нашего сервера, в реальном приложении делать этого, конечно, не нужно, вместо этого вам надо будет реализовать запрос на сервер, как это делать, надеюсь, вы знаете. Но в нашем случае использование такого фейкового сервера оправдано из соображений простоты.

**SimpleAuthService.java - реализация простейшего сервера**
```java
// Пропустим import'ы, чтобы уменьшить объем кода и облегчить его читаемость.
public class SimpleAuthService {
    private final Map<String, String> users;
    
    // Пропустим немного кода, который не влияет на понимание ситуации.
    
    // Метод для подделки вызова запроса авторизации.
    public boolean auth(String login, String password) {
        if (!users.containsKey(login)) {
            users.put(login, password);
            return true;
        } else {
            return users.get(login).equals(password);
        }
    }
}
```

Весь код я выложил на [гитхаб](https://github.com/konaire/FingerprintAuthentication). Для вашего удобства, код также был закоммичен частями, в удобном для его разбора порядке. Вот [здесь](https://github.com/konaire/FingerprintAuthentication/commit/a3058579cc7cda92fda88dde2e176027dd28c7f0) можно посмотреть инициализацию проекта и реализацию простейшего сервера.

## UI для сканирования отпечатка

Давайте теперь немного поверстаем. Для начала набросаем дизайн для активити, здесь нам нужно немного: `Button` для вызова диалога с предложением просканировать отпечаток и `TextView` для отображения статуса авторизации.

**activity_main.xml - xml c версткой для активити**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center">

    <TextView
        android:id="@+id/auth_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/auth_none"
        android:textColor="@android:color/black"
        android:textSize="@dimen/default_text_size" />

    <Button
        android:id="@+id/auth_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/default_gap"
        android:paddingStart="@dimen/default_gap"
        android:paddingEnd="@dimen/default_gap"
        android:textAllCaps="false"
        android:text="@string/auth_button"
        android:textColor="@android:color/black"
        android:textSize="@dimen/default_text_size" />
</LinearLayout>
```

Верстка довольно проста, поэтому давайте сразу перейдем к самому интересному - созданию диалогового окна для сканирования отпечатков. Для начала хорошо бы создать сам диалог, для этого надо добавить новый класс, сделать его наследником класса `DialogFragment` и перегрузить метод `onCreateDialog`, где нужно произвести инициализацию диалога, подгрузить к нему кастомный layout и добавить стандартную кнопку.

**FingerprintDialog.java - кастомный диалог для сканирования отпечатка**
```java
public class FingerprintDialog extends DialogFragment implements DialogInterface.OnClickListener {
    // Пропустим немного кода, который не влияет на понимание ситуации.

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_fingerprint, null, false);

        builder.setTitle(R.string.app_name)
            .setCancelable(true)
            .setView(view)
            .setNegativeButton(android.R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }
}
```

После этого создадим кастомный layout, который будет отображаться в диалоге.

**dialog_fingerprint.xml - кастомный layout для диалога**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="@dimen/dialog_padding"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    tools:ignore="UseCompoundDrawables">

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_fingerprint"
        android:contentDescription="@null" />

    <TextView
        android:id="@+id/dialog_message"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="@dimen/default_gap"
        android:text="@string/dialog_start_scanning_hint"
        android:textColor="@android:color/black"
        android:textSize="@dimen/default_text_size" />
</LinearLayout>
```

Ничего сложного, правда? Давайте теперь соберем все это и заставим работать, для этого нужно повесить отображение диалога при нажатии на кнопку в активити. Я не буду показывать как это сделать, данное действие довольно тривиально, надеюсь, вы справитесь с этим сами. В любом случае, весь код для данного этапа можно посмотреть [здесь](https://github.com/konaire/FingerprintAuthentication/commit/afe97547ff095a8c4031370343db48ba4c09c547). Давайте посмотрим, что у нас получилось?

<img alt="Диалоговое окно для сканирования отпечатков" src="https://raw.githubusercontent.com/konaire/FingerprintAuthentication/master/img/first.png" height="480"/>

_Диалоговое окно для сканирования отпечатков пальцев_

## Подготовка сканера отпечатков

Помимо обычных телефонов, которые поддерживают апи для отпечатков "из коробки", есть еще его величество Samsung, который начал встраивать сканер раньше, чем вышел Android 6.0. Они разработали свое апи, назвали его `Pass` и те устройства, что вышли до релиза Android M поддерживают только его. К их числу относится, например, довольно популярный Samsung Galaxy S5. Поэтому для того, чтобы обеспечить максимальную совместимость нашего приложения будем поддерживать обе реализации Fingerprint API. Для поддержки Samsung'ов скачайте `Pass` c [официального сайта](http://developer.samsung.com/galaxy/pass), распакуйте архив, jar'ы из Libs поместите в **{директория проекта}/app/libs**.

Для начала работы, как я уже говорил выше, нужно убедиться, что данное устройство поддерживает сканирование отпечатков, а также проверить, что блокировка экрана включена и хотя бы один отпечаток добавлен в систему. Давайте приступим к реализации этого функционала. Вместе с этим следует устанавливать правильное состояние для кнопки и статуса в `MainActivity`, полный код можно посмотреть [здесь](https://github.com/konaire/FingerprintAuthentication/commit/c671967f353220b800022b15d3ea73362528f722). А для краткости давайте сразу рассмотрим как будет происходить проверка.

**FingerprintApi.java - абстракция апи для проверки отпечатков пальцев**
```java
public interface FingerprintApi {
    int PERMISSION_FINGERPRINT = 100500; // Константа для запроса разрешений.

    boolean isFingerprintSupported(); // Метод для полной проверки доступности апи.
}
```

**MainActivity.java с функцией проверки доступности функционала сканирования отпечатков**
```java
private FingerprintApi api;

// Пропустим немного кода, который не влияет на понимание ситуации.

private boolean isFingerprintSupported() {
    FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
    Spass spassInstance = new Spass();

    try {
        // Вначале проверяем доступность стандартного апи, а затем самсунговского.
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            api = MarshmallowFingerprintApi.getInstance(this);
        } else {
            spassInstance.initialize(this);
            if (spassInstance.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)) {
                api = SamsungFingerprintApi.getInstance(this);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    // В конце проверим, что выполняются все дополнительные условия для конкретного апи.
    return api != null && api.isFingerprintSupported();
}
```

Мы создали скелет для работы с API отпечатков пальцев, давайте теперь реализуем 2 конкретные реализации: для обычного апи, которое предоставляет андроид и еще одно для самсунга.

**MarshmallowFingerprintApi.java - реализация стандартного апи для устройств на Android 6.0+**
```java
public final class MarshmallowFingerprintApi implements FingerprintApi {
    private final Activity activity;

    // Пропустим немного кода, который не влияет на понимание ситуации.

    @Override
    public boolean isFingerprintSupported() {
        KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Activity.KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Activity.FINGERPRINT_SERVICE);
        boolean hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.USE_FINGERPRINT }, PERMISSION_FINGERPRINT);
        }

        return hasPermission && keyguardManager != null && fingerprintManager != null &&
            keyguardManager.isKeyguardSecure() && fingerprintManager.hasEnrolledFingerprints();
    }
}
```

**SamsungFingerprintApi.java - реализация апи для некоторых моделей Samsung**
```java
public final class SamsungFingerprintApi implements FingerprintApi {
    private final class SamsungFingerprintHandler extends SpassFingerprint {
        SamsungFingerprintHandler(Context context) {
            super(context);
        }
    }

    private static final String PERMISSION = "com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY";

    private final Activity activity;
    private final SamsungFingerprintHandler fingerprintHandler;

    // Пропустим немного кода, который не влияет на понимание ситуации.

    @Override
    public boolean isFingerprintSupported() {
        if (ContextCompat.checkSelfPermission(activity, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { PERMISSION }, PERMISSION_FINGERPRINT);
            return false;
        } else {
            return fingerprintHandler.hasRegisteredFinger();
        }
    }
}
```

После этого шага, ваше приложение должно уметь определять устройства со сканером отпечатков и предлагать им пройти авторизацию, в случае, если у устройства нет сканера или отпечаток не задан, должна выводиться ошибка и блокироваться кнопка открытия диалога сканирования отпечатков.

## Реализация процесса сканирования

Здесь самое время немного остановиться и подумать о том как будет устроена наша авторизация со стороны клиента. Как уже было описано, для сервера нужен логин и пароль, но где их взять если мы сканируем отпечатки?

Рассмотрим процесс сканирование отпечатков поближе. И гугл, и самсунг в своем апи не дают разработчику возможности каким-либо образом считать данные, связанные с отпечатками пальцев пользователя. Это закрывает потенциальную дыру в безопасности, ~~фсб~~ злоумышленник не сможет снять цифровую копию вашего отпечатка, чтобы использовать ее в своих корыстных целях. Вместо этого нам предлагаются возможности асимметричного шифрования. Частью процесса авторизации является генерирование ключа шифрования, который в дальнейшем используется специальным шифровальщиком в момент авторизации. Такой ключ помещается в [специальное хранилище криптографических ключей](https://developer.android.com/training/articles/keystore.html), которое защищает ключи от доступа из вне, _запрещает их использование без авторизации_, а также позволяет накладывать ограничения на некоторые криптографические операции с ними.  Таким образом, при попытке доступа к ключу в случае успешной авторизации шифровальщик получает ключ и передает его назад в приложение, в противном случае он возвращает ошибку.

Очевидно, что данный ключ можно использовать как пароль. С логином все еще проще: в реальном приложении при авторизации он уже есть у пользователя, при регистрации пользователь его выбирает самостоятельно, в любом случае логин надо просто ввести. В нашем приложении давайте будем использовать "захардкоженную" строку: "konair@codebeavers.io". Вообще, для логина можно было использовать IMEI или генерировать уникальную строку и хранить ее в `Preferences`, но для простоты делать мы этого не будем. С паролем тоже все немного сложнее, более правильно было бы использовать не публичный ключ, а некоторый отпечаток, который зашифрован этим ключом, например отпечаток обычного пароля, пин кода или чего-то похожего. Но, в принципе, использование публичного ключа тоже допустимо.

Создадим еще один класс, который возьмет на себя обязанности по генерированию и проверке ключей. Пусть это будет `CryptoManager`. Итак для начала нужно сгенерировать ключ, который будет передаваться при авторизации. При инициализации мы указываем в какой кейстор нужно класть сгенерированные ключи и на каком алгоритме они работают.

**CryptoManager.generateKey() - метод для генерации криптографических ключей**
```java
private boolean generateKey() {
    try {
        // "AndroidKeyStore" - стандартный кейстор для андроид, который реализовывает все этапы защиты, которые были описаны выше.
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        
        // KEY_ALIAS - уникальный идентификатор вашего ключа.
        // KeyProperties.PURPOSE_DECRYPT достаточно потому, что ключу не нужно что-то шифровать/расшифровать.
        // Он будет использоваться только для подтверждения успешности авторизации. Для тех же целей нужен и вызов setUserAuthenticationRequired(true).
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setUserAuthenticationRequired(true);

        keyGenerator.initialize(builder.build());
        keyGenerator.generateKeyPair();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

Описанный ваше метод будет вызываться только в том случае, если `AndroidKeyStore` не содержит ключа для нашего приложения, давайте проверим это условие еще одним методом.

**CryptoManager.isKeyReady() - метод для проверки наличия ключа приложения в кейсторе**
```java
private boolean isKeyReady() {
    try {
        keyStore = KeyStore.getInstance("AndroidKeyStore");

        keyStore.load(null);
        return keyStore.containsAlias(KEY_ALIAS) || generateKey();
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

Мы проверили, что ключ есть в системе и создали его, если ключа не было. Это все? Нет. Помимо этого мы должны проверить ключ на валидность, т. к. в некоторых случаях ключ может стать не валидным, например, если пользователь сменил отпечаток пальца в системе. Я решил это сделать путем инициализации шифровальщика для расшифровки, поскольку реализация шифровальщика для зашифровки чуть сложнее, а сам шифровальщик может вам понадобиться, если вы захотите реализовать отправку не публичного ключа, а отпечатка чего-то. Ну и просто потому, что шифровальщик надо передавать в качестве аргумента при аутентификации через стандартное апи, но об этом чуть позднее.

**CryptoManager.isKeyValid() - метод для проверки валидности ключа шифрования**
```java
private boolean isKeyValid() {
    // Проверяем, что ключ создан и еще не проверен на валидность.
    if (!isKeyValid && isKeyReady()) {
        try {
            keyStore.load(null);

            // Создаем шифровальщик в соответствие с ключом: тот же алгоритм и параметры.
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/" + KeyProperties.BLOCK_MODE_ECB + "/" + KeyProperties.ENCRYPTION_PADDING_RSA_OAEP);
            OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            PrivateKey key = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);

            // Инициализируем шифровальщик ключом. Здесь же проверяется валидность ключа.
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            isKeyValid = true;
        } catch (Exception e) {
            // KeyPermanentlyInvalidatedException выбрасывается, если ключ не валиден, в этом случае заново создаем ключ.
            if (e instanceof KeyPermanentlyInvalidatedException && generateKey()) {
                return isKeyValid(); // И опять проверяем его на валидность.
            } else {
                e.printStackTrace();
            }
        }
    }

    return isKeyValid;
}
```

Все эти методы были `private`, а вот что мы позволим взять из нашего класса:

1. Публичный ключ, который будет использоваться как пароль при авторизации
2. Шифровальщик, т. к. он используется при аутентификации через стандартное апи

**Публичные методы для CryptoManager.java**
```java
// Метод возвращает валидный шифровальщик.
// Для этого он проходит все шаги по получению и проверке ключа.
Cipher getCipher() {
    return isKeyValid() ? cipher : null;
}

// Метод возвращает base64 представление для публичного ключа.
// Для этого он проходит все шаги по его получению и проверке.
String getPublicKey() {
    if (isKeyValid()) {
        PublicKey key;

        try {
            keyStore.load(null);
            key = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
            return new String(Base64.encode(key.getEncoded(), 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    return null;
}
```

Полный код для всех классов из этого параграфа можно посмотреть [здесь](https://github.com/konaire/FingerprintAuthentication/commit/061610cb805bce48fd7a186cb96334a14925a9bf), также в этом коммите был произведен небольшой рефакторинг кода. Мы реализовали всю логику по созданию, хранению и получению криптографических ключей, теперь можно приступать к самой авторизации. Давайте для начала добавим методы для запуска и отмены авторизации в наш интерфейс.

**Новые методы для интерфейса FingerprintApi.java**
```java
void start(); // Начать авторизацию по отпечатку пальца.
void cancel(); // Отменить авторизацию.
```

Эти методы как понятно из их названия будут отвечать за начало авторизации, когда сканер становится чувствительным к прикосновениям и на каждое из них считывает отпечаток пальца, сверяет его со значением, полученным при первоначальной настройке и "оглашает вердикт" подошел палец или нет. Также есть метод для отмены авторизации, в случае, если пользователь передумал авторизовываться, можно позвать этот метод и он запретит сканеру считывать отпечатки и передавать результат в приложение. Рассмотрим обе реализации апи. Начнем с апи для всех Android устройств.

**Реализация методов start() и cancel() для MarshmallowFingerprintApi.java**
```java
private CancellationSignal cancellationSignal; // используется для отмены авторизации

@Override
public void start() {
    cancellationSignal = new CancellationSignal();
    FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Activity.FINGERPRINT_SERVICE);
    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(CryptoManager.getInstance().getCipher());
    // Создаем/получаем ключ, проверяем его и возвращаем CryptoObject, который может быть использован для шифровки/дешифровки чего-либо.

    if (fingerprintManager != null) {
        // Производим аутентификацию для этого передаем new MarshmallowFingerprintHandler() в качестве callback'a.
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new MarshmallowFingerprintHandler(), null);
    }
}

@Override
public void cancel() {
    if (cancellationSignal != null) {
        cancellationSignal.cancel();
        cancellationSignal = null;
    }
}
```

В `SamsungFingerprintApi` вызовы  методов для запуска и отмены авторизации передаются в `SamsungFingerprintHandler`, в котором происходят все необходимые действия. Поэтому, чтобы не загромождать исходный класс, хэндлер теперь вынесен отдельным классом.

**SamsungFingerprintHandler.java - обработчик закрытых событий сканера отпечатков Samsung**
```java
class SamsungFingerprintHandler extends SpassFingerprint implements SpassFingerprint.IdentifyListener {
    private boolean isIdentifing; // true если авторизация запущена

    // Пропустим немного кода, который не влияет на понимание ситуации.

    @Override
    public void onFinished(int eventStatus) {
        // TODO: Здесь будем обрабатывать результаты.
        isIdentifing = false;
    }

    void start() {
        if (isIdentifing) {
            return;
        }

        try {
            // Создаем/получаем ключ и проверяем его на валидность.
            boolean hasValidKey = CryptoManager.getInstance().getCipher() != null;
            
            if (hasValidKey) {
                isIdentifing = true;
                startIdentify(this);
            }
        } catch (Exception e) {
            // Иногда авторизация застревает в предыдущем состоянии, надо немного подождать...
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, 2 * 1000);
            isIdentifing = false;
        }
    }

    void cancel() {
        if (isIdentifing) {
            cancelIdentify();
            isIdentifing = false;
        }
    }
}
```

**Внимание!** Вызов метода `start()` может привести к зависанию ui потока, особенно при генерации криптографического ключа, лучше всего этот метод вызывать из отдельного потока, желательно, при этом показать пользователю какую-нибудь крутилку, чтобы отразить ожидание в интерфейсе. Сделать это довольно просто, поэтому для упрощения кода данный функционал я не реализовывал. 

## Обработка результатов

Вы еще помните, что нам нужно произвести авторизацию на сервере, передав туда логин и публичный ключ? Для этого хорошо бы создать свой интерфейс-обработчик результатов сканирования отпечатков, поскольку разные имплементации реализовывают передачу результатов по-разному, а через общий интерфейс мы это все унифицируем. Реализацию этого интерфейса нужно передать в метод `start()` нашего апи.

**FingerprintApi.Callback - новый интерфейс для обработки результатов сканирования**
```java
public abstract class FingerprintApi {
    // Общий обработчик результатов сканирования для всех апи.
    public interface Callback {
        // Вызывается, если отпечаток пальца успешно распознан.
        // Передается base64 представление публичного ключа.
        void onSuccess(String publicKey);

        // Вызывается, если отпечаток пальца не распознан.
        void onFailure();

        // Вызывается, если в процессе сканирования произошла ошибка, передается код ошибки.
        void onError(int errorCode);
    }

    // Пропустим немного кода, который не влияет на понимание ситуации.

    public abstract void start(@NonNull Callback callback);
}
```

Внутри каждой конкретной реализации callback дальше передается в соответствующий handler. Полный код для данного этапа можно посмотреть [в этом коммите](https://github.com/konaire/FingerprintAuthentication/commit/bbd05a95676701e44404c04b93f150abdcdf65cb). А дальше давайте посмотрим как в хэндлерах реализована передача результатов.

**MarshmallowFingerprintHandler.java - обработчик результатов сканирования для дефолтного API**
```java
class MarshmallowFingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private final FingerprintApi.Callback callback;

    MarshmallowFingerprintHandler(FingerprintApi.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        callback.onSuccess(CryptoManager.getInstance().getPublicKey());
    }

    @Override
    public void onAuthenticationFailed() {
        callback.onFailure();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errorString) {
        if (errorCode != FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED) {
            callback.onError(errorCode);
        }
    }
}
```

**SamsungFingerprintHandler.java - обработчик результатов сканирования для API от Samsung**
```java
class SamsungFingerprintHandler extends SpassFingerprint implements SpassFingerprint.IdentifyListener {
    private FingerprintApi.Callback callback;

    // Пропустим немного кода, который не влияет на понимание ситуации.

    @Override
    public void onFinished(int eventStatus) {
        if (callback != null) {
            switch (eventStatus) {
                case STATUS_AUTHENTIFICATION_SUCCESS:
                    callback.onSuccess(CryptoManager.getInstance().getPublicKey()); break;
                case STATUS_USER_CANCELLED:
                    break; // ничего не делаем
                case STATUS_QUALITY_FAILED:
                case STATUS_AUTHENTIFICATION_FAILED:
                    callback.onFailure(); break;
                default:
                    callback.onError(eventStatus); break;
            }
        }

        isIdentifing = false;
    }

    void setCallback(FingerprintApi.Callback callback) {
        this.callback = callback;
    }
}
```

Как видим в обоих случаях внутренняя обработка результатов соотносится с методами для `FingerprintApi.Callback`, а также, в случае отмены пользователем сканирования, генерируемая ошибка игнорируется. На мой взгляд правильнее делать именно так, поскольку отмена сканирования в нашем случае реализована кнопкой и вызов метода для обработки ошибки в этом случае не нужен. Также хочу обратить ваше внимание, что в методе `onError` передается числовой статус ошибки, в соответствие с ним вы можете показывать более детальное сообщение об ошибке, ну или просто передавать код, как это сделал я. Список всех кодов находится в классах: FingerprintManager для андроидовского апи, SpassFingerprint для самсунговского апи.

Приводить реализацию callback'a в коде я не буду, опишу словами. `FingerprintApi.Callback` реализуется в диалоге, на каждое событие реагирует UI, изменяется иконка и текст. Из диалога передается еще один callback с одним методом, который реализуется в активити и вызывается, если отпечаток пальца успешно распознан, с передачей туда публичного ключа. Здесь же вызывается метод для авторизации на нашем простом сервере, а чтобы этот процесс был чуть сложнее в сервер уже добавлен еще один пользователь и на основе рандома логин выбирается то правильный, то нет, таким образом можно убедиться, что авторизация действительно работает.

<img alt="Диалоговое окно для сканирования отпечатков" src="https://raw.githubusercontent.com/konaire/FingerprintAuthentication/master/img/second.png" height="480"/> <img alt="Диалоговое окно для сканирования отпечатков" src="https://raw.githubusercontent.com/konaire/FingerprintAuthentication/master/img/third.png" height="480"/>

_Скриншоты приложения на разных стадиях авторизации_

На этом все, спасибо за внимание.

P.S. Весь код доступен в репозитории на [гитхабе](https://github.com/konaire/FingerprintAuthentication). А все этапы разбиты [по коммитам](https://github.com/konaire/FingerprintAuthentication/commits/master).
