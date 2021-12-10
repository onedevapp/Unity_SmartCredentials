using System.Collections;
using System.Collections.Generic;
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
        public Button VerifyOtpBtn;
        public Button resendOtpBtn;
        public Button cancelOtpBtn;

        public InputField emailInput;
        public InputField passwordInput;
        public InputField mobileNoInput;
        public InputField otpVerifyInput;

        public GameObject MobileHintGO;
        public GameObject OTPVerifyGO;

        public Text optTimerTxt;
        public Text outputLogTxt;


        // Start is called before the first frame update
        void Start()
        {
            DeleteCredentialBtn.interactable = false;
            OTPVerifyGO.SetActive(false);
            resendOtpBtn.gameObject.SetActive(false);
            cancelOtpBtn.gameObject.SetActive(false);
            GetMobileHintBtn.interactable = true;
            optTimerTxt.text = "00:00";

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
                    if(status == (int)AuthErrorCode.AUTH_HINTS_AVAILABLE)
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
                    if(status == (int)AuthErrorCode.AUTH_HINTS_AVAILABLE)
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
                MobileHintGO.SetActive(false);
                OTPVerifyGO.SetActive(true);
                mobileNoInput.interactable = false;
                GetMobileHintBtn.interactable = false;

                SmartCredentialsManager.Instance.StartListeningForOTP((otpValue, status)=> {
                    if(status == (int)OTPErrorCode.SMS_RECEIVER_OPT_RECEIVED)
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
                cancelOtpBtn.gameObject.SetActive(false);
                resendOtpBtn.gameObject.SetActive(false);

                SmartCredentialsManager.Instance.StartListeningForOTP((otpValue, status)=> {
                    if(status == (int)OTPErrorCode.SMS_RECEIVER_OPT_RECEIVED)
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

                MobileHintGO.SetActive(true);
                OTPVerifyGO.SetActive(false);

                mobileNoInput.interactable = true;
                GetMobileHintBtn.interactable = true;

                cancelOtpBtn.gameObject.SetActive(false);
                resendOtpBtn.gameObject.SetActive(false);
            });


            VerifyOtpBtn.onClick.AddListener(() => {
                SmartCredentialsManager.Instance.SaveToCredentials(mobileNoInput.text, "", App_IdentityProviders, "", "", isSuccess => {
                    outputLogTxt.text = "Save Credential - " + isSuccess;
                });
            });
        }

    }

}