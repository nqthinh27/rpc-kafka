package vn.com.rpckafka.constant;

public class ConnectorStatus {

//    The connector/task has not yet been assigned to a worker.
    public static final String UNASSIGNED = "UNASSIGNED";

//    The connector/task is running.
    public static final String RUNNING = "RUNNING";

//    The connector/task has been administratively paused.
    public static final String PAUSED = "PAUSED";

//    The connector/task has failed (usually by raising an exception, which is reported in the status output).
    public static final String FAILED = "FAILED";
}
