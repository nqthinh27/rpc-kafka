package vn.com.rpckafka.model;

import java.util.List;

public class KafkaConnectStatusInfo {

    private KafkaConnectStatusDetail status;

    public KafkaConnectStatusDetail getStatus() {
        return status;
    }

    public void setStatus(KafkaConnectStatusDetail status) {
        this.status = status;
    }

    public class KafkaConnectStatusDetail {
        private String name;
        private Connector connector;
        private List<ConnectorTask> tasks;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Connector getConnector() {
            return connector;
        }

        public void setConnector(Connector connector) {
            this.connector = connector;
        }

        public List<ConnectorTask> getTasks() {
            return tasks;
        }

        public void setTasks(List<ConnectorTask> tasks) {
            this.tasks = tasks;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
