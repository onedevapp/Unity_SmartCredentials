# Unity Smart Credential
Unity plugin integration with Google Smart Lock and SMS Verification APIs for Android
<br><br>

### INSTALLATION
There are 4 ways to install this plugin:

1. import Unity_SmartCredential.unitypackage via Assets-Import Package
2. clone/download this repository and move the Plugins folder to your Unity project's Assets folder
3. via Package Manager (**Add package from git url**):

    - `https://github.com/onedevapp/Unity_SmartCredential.git`
4. via Package Manager (add the following line to **Packages/manifest.json**):
    - `"com.onedevapp.smartcredentials": "https://github.com/onedevapp/Unity_SmartCredential.git",`

<br>

### Requirements
* You project should build against Android 5.0 (API level 21) SDK at least.
* This plugin uses a custom tool for dependency management called the [Play Services Resolver](https://github.com/googlesamples/unity-jar-resolver)


### How To
Functionality included: SmartLock Credentials Request, SmartLock Credentials Save, SmartLock Credentials Delete, SMS Retriever Client to retrive SMS for your APP without need permission of SMS_READ

* ### Smart Lock for Passwords
The following diagram shows the flow of a typical Android app that uses Smart Lock for Passwords.
<br>
<img src="https://developers.google.com/identity/smartlock-passwords/android/images/smartlock-passwords-flow.png" width="800"/>

Source: [developers.google.com](https://developers.google.com/identity/smartlock-passwords/android/images/smartlock-passwords-flow.png)
<br>

### API 
-	Obtain the user's email address
	```C#
	SmartCredentialsManager.Instance.RequestEmailAddressHint(Action<string, int> OnEmailHintAction, params string[] accountTypes)
	```
-	To retrieve credentials
	```C#
	SmartCredentialsManager.Instance.GetSavedCredentials(Action<string, string, string, int> OnRetrievedAction, params string[] accountTypes)
	```
-	To save credentials
	```C#
	SmartCredentialsManager.Instance.SaveToCredentials(string emailOrMobile, string password, string accountType, string displayName, string profilePicUrl, Action<bool> OnSavedAction)
	```
-	To delete a saved credentials
	```C#
	SmartCredentialsManager.Instance.DeleteSavedCredentials(Action<bool> OnDeleteAction)
	```
-	Available Account Type Identity
	```C#
	SmartCredentialsManager.IdentityProviders_FACEBOOK 
	SmartCredentialsManager.IdentityProviders_GOOGLE
	SmartCredentialsManager.IdentityProviders_LINKEDIN 
	SmartCredentialsManager.IdentityProviders_MICROSOFT
	SmartCredentialsManager.IdentityProviders_PAYPAL
	SmartCredentialsManager.IdentityProviders_TWITTER
	SmartCredentialsManager.IdentityProviders_YAHOO
	```
<br>

<img src="https://developers.google.com/identity/smartlock-passwords/android/images/netflix-save.png" height="400"/>
<img src="https://developers.google.com/identity/smartlock-passwords/android/images/netflix-retrieve.png" height="400"/>

Source: [developers.google.com](https://developers.google.com/identity/smartlock-passwords/android/images/netflix-save.png)
<br><br>

* ### Automatic SMS Verification with the SMS Retriever API
As per Google's new policy with the SMS Retriever API, you can perform SMS-based user verification in your Android app automatically, without requiring the user to manually type verification codes, and without requiring any extra app permissions.

#### Warning as per the new policy
- Google restricts which Android apps can request Call Log and SMS permissions 
- Only apps selected as the device's default app for making calls or sending text messages will be able to access call logs and SMS data from now on.
<br><br>

SMS verification flow looks like this:

<img src="https://developers.google.com/identity/sms-retriever/flow-overview.png" width="800"/>

Source: [developers.google.com](https://developers.google.com/identity/sms-retriever/flow-overview.png)
<br>

### API 
-	Obtain the user's phone number
	```C#
	SmartCredentialsManager.Instance.RequestMobileNoHint(Action<string, int> OnMobileHintAction)
	```
-	Start the SMS retriever
	```C#
	SmartCredentialsManager.Instance.StartListeningForOTP(Action<string, int> OnOTPFetchedAction, string regexOTPPattern = "")
	```
-	Stop the retriever for listining
	```C#
	//Plugin automatically stops listening for OTP on every action, call this function only when user leaves in the middle of the process
	SmartCredentialsManager.Instance.StopListeningForOTP()
	```

### Construct a verification message
When your server receives a request to verify a phone number, first construct the verification message that you will send to the user's device. This message must:

* Be no longer than 140 bytes
* Contain a one-time code that the client sends back to your server to complete the verification flow
* Include an 11-character hash string that identifies your app

For example, a valid verification message might look like the following:

```
Your ExampleApp code is: <Verification Code>

<Key Hash string>
```
#### Computing your app's hash string
To generate your app's hash string:
* [From keyStore](https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string)
* 	```C#
	//This works only when the plugin debug is set true and hash string will be printed on console
	SmartCredentialsManager.Instance.GetAppSignatures();
	//Inside logcat: AppSignatureHelper pkg: packagename -- hash: <Key Hash string>
	```

#### Verification Code Pattern
This plugin retrieve the verification code using pattern which matches. Default pattern is ```(\d{4}) //Four digit OTP``` .
-	Can be overwritten during StartListeningForOTP
	```C#
	string regexOTPPattern = "(\d{6})";	//Six digit OTP
	SmartCredentialsManager.Instance.StartListeningForOTP((otpValue, status)=> {}, regexOTPPattern);
	```

* ### Debug
Toggle library logs

```C#
//By default puglin console log will be diabled, but can be enabled
SmartCredentialsManager.Instance.PluginDebug(bool showLog);
```
<br>

## Libraries
- #### [UnityMainThreadDispatcher](https://github.com/PimDeWitte/UnityMainThreadDispatcher)
<br>

## :open_hands: Contributions
Any contributions are welcome!

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create New Pull Request

<br><br>