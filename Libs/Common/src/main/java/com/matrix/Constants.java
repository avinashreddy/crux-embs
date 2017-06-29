package com.matrix;

public final class Constants {
    /**
     * The default host name.
     * @see #SERVER_HOST_NAME
     */
    public static final String DEFAULT_HOST_NAME = "localhost";
    /**
     * The env variable that hold the server environment name - DEV, QA, UAT, PROD etc
     */
    public static final String SERVER_ENV = "SERVER_ENV";
    /**
     * The env variable that hold the docker container hostname (docker run -e HOST_NAME=<NAME>)
     */
    public static final String SERVER_HOST_NAME = "SERVER_HOST_NAME";
    /**
     * If true then email subject is prefixed by {@link #SERVER_ENV}.
     *
     * default is true.
     */
    public static final String PREFIX_ENV_TO_MAIL_SUBJECT = "PREFIX_ENV_TO_MAIL_SUBJECT";
    /**
     * If true then {@link #SERVER_HOST_NAME} is prefixed to the message body if the mesage body does not contain the
     * host name. We want all messages to contain host name. This is intended to spot accidental omission of
     * host name from the message.
     *
     * default is true.
     */
    public static final String INJECT_HOST_NAME_IN_MESSAGE = "INJECT_HOST_NAME_IN_MESSAGE";

    public static final String STEP_NAME = "STEPNAME";

    public static final String JOB_NAME = "JOBNAME";


    private Constants() {}

}
