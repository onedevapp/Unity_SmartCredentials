namespace OneDevApp.SmartCredentials
{
    public enum AuthErrorCode
    {
        AUTH_HINTS_AVAILABLE = 0,
        AUTH_UNAVAILABLE_PLAY_SERVICE_ERROR = 1,
        AUTH_PLAY_SERVICE_UNSUPPORTED_VERSION_ERROR = 2,
        AUTH_NO_HINTS_AVAILABLE = 3,
        AUTH_INTERNAL_ERROR = 4
    }

    public enum OTPErrorCode
    {
        SMS_RECEIVER_OPT_RECEIVED = 0,
        SMS_RECEIVER_ERROR = 1,
        SMS_RECEIVER_TIMEOUT = 2,
        SMS_RECEIVER_OPT_PARSE_ERROR = 3
    }

    public enum ReadCredentialErrorCode
    {
        READ_CREDENTIAL_AVAILABLE = 0,
        READ_NO_CREDENTIAL_AVAILABLE = 3,
    }
}