using System;
using System.Collections;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using UnityEngine;

namespace OneDevApp.SmartCredentials
{
    public class SmartCredentialsManager : MonoBehaviour
    {
        #region Constants

        public const string IdentityProviders_FACEBOOK = "https://www.facebook.com";
        public const string IdentityProviders_GOOGLE = "https://accounts.google.com";
        public const string IdentityProviders_LINKEDIN = "https://www.linkedin.com";
        public const string IdentityProviders_MICROSOFT = "https://login.live.com";
        public const string IdentityProviders_PAYPAL = "https://www.paypal.com";
        public const string IdentityProviders_TWITTER = "https://twitter.com";
        public const string IdentityProviders_YAHOO = "https://login.yahoo.com";

        #endregion

        #region Events

#pragma warning disable 0067
        /// <summary>
        /// Event triggered when the Credentials Status of Save or Delete completed
        /// </summary>
        private static event Action<bool> OnCredentialsStatusAction;
        /// <summary>
        /// Event triggered when user selected any hint value or  when otp received
        /// </summary>
        private static event Action<string, int> OnResultAction;
        /// <summary>
        /// Event triggered when user selected any hint value or  when otp received
        /// </summary>
        private static event Action<string, int> OnOtpAction;
        /// <summary>
        /// Event triggered with user credential details
        /// </summary>
        private static event Action<string, string, string, int> OnCredentialsRetrievedAction;

#pragma warning restore 0067
        #endregion

        public static SmartCredentialsManager Instance { get; private set; }

#pragma warning disable 0414
        /// <summary>
        /// UnityMainActivity current activity name or main activity name
        /// Modify only if this UnityPlayer.java class is extends or used any other default class
        /// </summary>
        [Tooltip("Android Launcher Activity")]
        [SerializeField]
        private string m_unityMainActivity = "com.unity3d.player.UnityPlayer";

        public string RegexPattern { get; set; }
        public bool DebugMode { get; private set; }

#pragma warning restore 0414


#if UNITY_ANDROID && !UNITY_EDITOR
        private AndroidJavaObject mContext = null;
        private static AndroidJavaObject credentialTempObj = null;

        class OnResultListener : AndroidJavaProxy
        {
            public OnResultListener() : base("com.onedevapp.smartcredentials.listeners.OnResultListener") { }

            public void onResult(string message, int code)
            {
                UnityMainThreadDispatcher.Instance().Enqueue(() => {
                    OnResultAction?.Invoke(message, code);
                    OnResultAction = null;
                });
            }
        }

        class OnCredentialsStatusListener : AndroidJavaProxy
        {
            public OnCredentialsStatusListener() : base("com.onedevapp.smartcredentials.listeners.OnCredentialsStatusListener") { }

            public void onCredentialsStatus(bool isSuccess)
            {
                UnityMainThreadDispatcher.Instance().Enqueue(() => {
                    OnCredentialsStatusAction?.Invoke(isSuccess);
                    OnCredentialsStatusAction = null;
                });
            }
        }

        class OnCredentialsResultListener : AndroidJavaProxy
        {
            public OnCredentialsResultListener() : base("com.onedevapp.smartcredentials.listeners.OnCredentialsResultListener") { }

            public void onCredentialsResult(AndroidJavaObject credentialObj, int statusCode)
            {
                credentialTempObj = credentialObj;
                UnityMainThreadDispatcher.Instance().Enqueue(() => {
                    if(statusCode == 0){
                        // When running `AndroidJavaObject` methods, you need to provide a type for the value to be assigned to
		                var userID = credentialObj.Call<string>("getId");
		                var password = credentialObj.Call<string>("getPassword");
		                var accountType = credentialObj.Call<string>("getAccountType");
                        OnCredentialsRetrievedAction?.Invoke(userID, password, accountType, statusCode);
                        OnCredentialsRetrievedAction = null;
                    }else{
                        credentialTempObj = null;
                        OnCredentialsRetrievedAction?.Invoke("", "", "", statusCode);
                        OnCredentialsRetrievedAction = null;
                    }
                });
            }
        }
#endif

        private void Awake()
        {
            if (Instance == null)
            {
                Instance = this;
            }
            else
            {
                DestroyImmediate(Instance.gameObject);
                Instance = this;
            }

#if UNITY_ANDROID && !UNITY_EDITOR
            if (Application.platform == RuntimePlatform.Android)
            {
                mContext = new AndroidJavaClass(m_unityMainActivity).GetStatic<AndroidJavaObject>("currentActivity");
            }
#elif UNITY_EDITOR
            Debug.Log("Platform not supported");
#endif
        }

        #region SmartLock
        public void RequestMobileNoHint(Action<string, int> OnMobileHintAction)
        {
            if (OnResultAction != null)
                OnResultAction = null;

            OnResultAction = OnMobileHintAction;

#if UNITY_ANDROID && !UNITY_EDITOR

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("RequestMobileNoHint", new OnResultListener());
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void RequestEmailAddressHint(Action<string, int> OnEmailHintAction, params string[] accountTypes)
        {
            if (OnResultAction != null)
                OnResultAction = null;

            OnResultAction = OnEmailHintAction;

#if UNITY_ANDROID && !UNITY_EDITOR

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("RequestEmailAddressHint", javaArrayFromCS(accountTypes), new OnResultListener());
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void StartListeningForOTP(Action<string, int> OnOTPFetchedAction, string regexOTPPattern = @"([0-9]{4})")
        {
            if (OnOtpAction != null)
                OnOtpAction = null;

            OnOtpAction = OnOTPFetchedAction;
            RegexPattern = regexOTPPattern;

#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("StartListening");
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void StopListeningForOTP()
        {
            OnOtpAction = null;
#if UNITY_ANDROID && !UNITY_EDITOR

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("StopListening");
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void GetSavedCredentials(Action<string, string, string, int> OnRetrievedAction, params string[] accountTypes)
        {

            if (OnCredentialsRetrievedAction != null)
                OnCredentialsRetrievedAction = null;

            OnCredentialsRetrievedAction = OnRetrievedAction;

#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("GetStoredCredentials", javaArrayFromCS(accountTypes), new OnCredentialsResultListener());
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void SaveToCredentials(string emailOrMobile, string password, string accountType, string displayName, string profilePicUrl, Action<bool> OnSavedAction)
        {

            if (string.IsNullOrEmpty(emailOrMobile))
            {
                Debug.Log("Email id or Mobile No cant be empty");
                OnSavedAction?.Invoke(false);
                return;
            }

            if (OnCredentialsStatusAction != null)
                OnCredentialsStatusAction = null;

            OnCredentialsStatusAction = OnSavedAction;

#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("SaveToStoredCredentials", emailOrMobile, password, accountType, displayName, profilePicUrl, new OnCredentialsStatusListener());
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

        public void DeleteSavedCredentials(Action<bool> OnDeleteAction)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (credentialTempObj == null)
            {
                Debug.Log("Credential to delete, cant be null");
                OnDeleteAction?.Invoke(false);
                return;
            }

            if (OnCredentialsStatusAction != null)
                OnCredentialsStatusAction = null;
                
            OnCredentialsStatusAction = (isSuccess)=> {
                credentialTempObj = null;
                OnDeleteAction?.Invoke(isSuccess);
                OnCredentialsStatusAction = null;
            };

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("DeleteStoredCredentials", credentialTempObj, new OnCredentialsStatusListener());
            }
#endif
        }

        public void GetAppSignatures()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.smartcredentials.AuthManager"))
            {
                var mAuthManager = jc.CallStatic<AndroidJavaObject>("getInstance");
                mAuthManager.Call<AndroidJavaObject>("setActivity", mContext);
                mAuthManager.Call("GetAppSignatures");
            }
#elif UNITY_EDITOR
            if (DebugMode)
                Debug.Log("Platform not supported");
#endif
        }

#endregion


        #region Debug
        /// <summary>
        /// By default puglin console log will be diabled, but can be enabled
        /// </summary>
        /// <param name="showLog">If set true then log will be displayed else disabled</param>
        public void PluginDebug(bool showLog = true)
        {
            DebugMode = showLog;

#if UNITY_ANDROID && !UNITY_EDITOR

            AndroidJNIHelper.debug = showLog;
            var constantClass = new AndroidJavaClass("com.onedevapp.smartcredentials.utilities.Constants");
            constantClass.SetStatic("enableLog", showLog);

/*#elif UNITY_EDITOR
            DebugMode = showLog;*/
#endif
        }
        #endregion

#if UNITY_ANDROID && !UNITY_EDITOR
        /// <summary>
        /// Converts Csharp array to java array
        /// https://stackoverflow.com/questions/42681410/androidjavaobject-call-array-passing-error-unity-for-android
        /// </summary>
        /// <param name="values"></param>
        /// <returns></returns>
        private AndroidJavaObject javaArrayFromCS(string[] values)
        {
            AndroidJavaClass arrayClass = new AndroidJavaClass("java.lang.reflect.Array");
            AndroidJavaObject arrayObject = arrayClass.CallStatic<AndroidJavaObject>("newInstance", new AndroidJavaClass("java.lang.String"), values.Length);
            for (int i = 0; i < values.Length; ++i)
            {
                arrayClass.CallStatic("set", arrayObject, i, new AndroidJavaObject("java.lang.String", values[i]));
            }

            return arrayObject;
        }
        
#endif

        void OnOtpReceiveSuccess(string value)
        {
            if (OnOtpAction != null)
            {
                var match = Regex.Match(value, RegexPattern);
                Debug.Log(match.Success);
                if (match.Success)
                {
                    OnOtpAction.Invoke(match.Value, (int)OTPErrorCode.SMS_RECEIVER_OPT_RECEIVED);
                }
                else
                {
                    OnOtpAction.Invoke("", (int)OTPErrorCode.SMS_RECEIVER_OPT_PARSE_ERROR);
                }
            }
            StopListeningForOTP();
        }
        
        void OnOtpReceiveError(string value)
        {
            if (OnOtpAction != null)
            {
                OnOtpAction.Invoke("", int.Parse(value));
            }
            StopListeningForOTP();
        }
    }

}