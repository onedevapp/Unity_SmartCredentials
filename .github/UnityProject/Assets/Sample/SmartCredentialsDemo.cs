using UnityEngine;
using UnityEngine.UI;

namespace OneDevApp.SmartCredentials.Demo
{
    public class SmartCredentialsDemo : MonoBehaviour
    {
        public const string App_IdentityProviders = "https://signin.example.com";

        public Button SaveCredentialBtn;
        public Button DeleteCredentialBtn;
        public Button LoadCredentialBtn;
        public Button GetEmailHintBtn;
        public Button GetMobileHintBtn;
        public Button GetOtpBtn;
        public Button VerifyOtpNSaveMobileLoginBtn;
        public Button resendOtpBtn;
        public Button cancelOtpBtn;

        public InputField emailInput;
        public InputField passwordInput;
        public InputField mobileNoInput;
        public InputField otpVerifyInput;

        public GameObject MobileHintGO;
        public GameObject OTPVerifyGO;

        public float timeRemaining = 10;
        public bool timerIsRunning = false;
        public Text otpTimerTxt;
        public Text outputLogTxt;


        // Start is called before the first frame update
        void Start()
        {
            DeleteCredentialBtn.interactable = false;
            OTPVerifyGO.SetActive(false);
            resendOtpBtn.gameObject.SetActive(false);
            cancelOtpBtn.gameObject.SetActive(false);
            GetMobileHintBtn.interactable = true;
            otpTimerTxt.text = "00:00";

            SmartCredentialsManager.Instance.PluginDebug();

            if (SmartCredentialsManager.Instance.DebugMode)
                SmartCredentialsManager.Instance.GetAppSignatures();

            DeleteCredentialBtn.onClick.AddListener(()=> {
                SmartCredentialsManager.Instance.DeleteSavedCredentials(isSuccess=> {
                    outputLogTxt.text = "Delete Credential - " + isSuccess;
                });
            });

            SaveCredentialBtn.onClick.AddListener(()=> {
                SmartCredentialsManager.Instance.SaveToCredentials(emailInput.text, passwordInput.text, "", "","", isSuccess=> {
                    outputLogTxt.text = "Save Credential - " + isSuccess;
                });
            });

            LoadCredentialBtn.onClick.AddListener(()=> {
                SmartCredentialsManager.Instance.GetSavedCredentials((email, password, accountType, status)=> {
                    
                    if(status == (int) ReadCredentialErrorCode.READ_CREDENTIAL_AVAILABLE)
                    {
                        emailInput.text = email;
                        passwordInput.text = password;
                        DeleteCredentialBtn.interactable = true;
                    }
                    else
                    {
                        DeleteCredentialBtn.interactable = false;
                        outputLogTxt.text = "No stored credentials found";
                    }
                }, "", App_IdentityProviders);
            });

            GetEmailHintBtn.onClick.AddListener(()=> {
                SmartCredentialsManager.Instance.RequestEmailAddressHint((email, status)=> {
                    SmartCredentialsManager.Instance.RemoveHintAction();
                    if (status == (int)AuthErrorCode.AUTH_HINTS_AVAILABLE)
                    {
                        emailInput.text = email;
                    }
                    else
                    {
                        outputLogTxt.text = "No email hint found";
                    }
                }, App_IdentityProviders, SmartCredentialsManager.IdentityProviders_GOOGLE);
            });

            GetMobileHintBtn.onClick.AddListener(()=> {
                SmartCredentialsManager.Instance.RequestMobileNoHint((mobileNo, status)=> {
                    SmartCredentialsManager.Instance.RemoveHintAction();
                    if (status == (int)AuthErrorCode.AUTH_HINTS_AVAILABLE)
                    {
                        mobileNoInput.text = mobileNo;
                    }
                    else
                    {
                        outputLogTxt.text = "No mobile hint found";
                    }
                });
            });

            GetOtpBtn.onClick.AddListener(()=> {

                ///Do these functionalites only after success response from your server for sending an OTP to this user

                // Starts the timer automatically
                timeRemaining = 10;
                timerIsRunning = true;

                MobileHintGO.SetActive(false);
                OTPVerifyGO.SetActive(true);
                mobileNoInput.interactable = false;
                GetMobileHintBtn.interactable = false;
                cancelOtpBtn.gameObject.SetActive(true);

                SmartCredentialsManager.Instance.StartListeningForOTP((otpValue, status)=> {
                    timerIsRunning = false;
                    if (status == (int)OTPErrorCode.SMS_RECEIVER_OPT_RECEIVED)
                    {
                        otpVerifyInput.text = otpValue;
                    }
                    else if (status == (int)OTPErrorCode.SMS_RECEIVER_TIMEOUT)
                    {
                        outputLogTxt.text = "OTP TIme out";
                        resendOtpBtn.gameObject.SetActive(true);
                        cancelOtpBtn.gameObject.SetActive(true);
                    }
                    else
                    {
                        mobileNoInput.interactable = true;
                        GetMobileHintBtn.interactable = true;
                        outputLogTxt.text = "unable to fetch OTP";
                    }
                });
            });

            resendOtpBtn.onClick.AddListener(()=> {

                ///Do these functionalites only after success response from your server for resending an OTP to this user
                
                cancelOtpBtn.gameObject.SetActive(true);
                resendOtpBtn.gameObject.SetActive(false);
                SmartCredentialsManager.Instance.StopListeningForOTP();

                // Starts the timer automatically
                timeRemaining = 10;
                timerIsRunning = true;

                SmartCredentialsManager.Instance.StartListeningForOTP((otpValue, status)=> {
                    timerIsRunning = false;
                    if (status == (int)OTPErrorCode.SMS_RECEIVER_OPT_RECEIVED)
                    {
                        otpVerifyInput.text = otpValue;
                    }
                    else if (status == (int)OTPErrorCode.SMS_RECEIVER_TIMEOUT)
                    {
                        outputLogTxt.text = "OTP TIme out";
                        cancelOtpBtn.gameObject.SetActive(true);
                    }
                    else
                    {
                        outputLogTxt.text = "unable to fetch OTP";
                    }
                });
            });

            cancelOtpBtn.onClick.AddListener(()=> {
                
                timerIsRunning = false;

                SmartCredentialsManager.Instance.StopListeningForOTP();
                MobileHintGO.SetActive(true);
                OTPVerifyGO.SetActive(false);

                mobileNoInput.interactable = true;
                GetMobileHintBtn.interactable = true;

                cancelOtpBtn.gameObject.SetActive(false);
                resendOtpBtn.gameObject.SetActive(false);
            });


            VerifyOtpNSaveMobileLoginBtn.onClick.AddListener(() => {

                ///Do these functionalites only after success response from your server for successfully verifying OTP to this user
                
                SmartCredentialsManager.Instance.SaveToCredentials(mobileNoInput.text, "", App_IdentityProviders, "", "", isSuccess => {
                    outputLogTxt.text = "Save Credential - " + isSuccess;
                });
            });
        }

        private void Update()
        {
            /*if (Input.GetKeyDown(KeyCode.Space))
            {
                var match = Regex.Match("You OTP is : 1963    L5Qy+JE8Crk", @"([0-9]{4})");
                Debug.Log(match.Success);
                if (match.Success)
                {
                    Debug.Log(match.Value);
                }
            }*/
            if (timerIsRunning)
            {
                if (timeRemaining > 0)
                {
                    timeRemaining -= Time.deltaTime;
                    DisplayTime(timeRemaining);
                }
                else
                {
                    Debug.Log("Time has run out!");
                    timeRemaining = 0;
                    timerIsRunning = false;

                    outputLogTxt.text = "OTP TIme out";
                    resendOtpBtn.gameObject.SetActive(true);
                    cancelOtpBtn.gameObject.SetActive(false);
                }
            }
        }
        void DisplayTime(float timeToDisplay)
        {
            timeToDisplay += 1;
            float minutes = Mathf.FloorToInt(timeToDisplay / 60);
            float seconds = Mathf.FloorToInt(timeToDisplay % 60);
            otpTimerTxt.text = string.Format("{0:00}:{1:00}", minutes, seconds);
        }

    }

}