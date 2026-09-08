package io.github.mrergos.workinghours.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jms")
public class JmsQueueProperties {

    private Queues queues = new Queues();
    private Listener listener = new Listener();

    public Queues getQueues() {
        return queues;
    }

    public void setQueues(Queues queues) {
        this.queues = queues;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static class Queues {
        private String workloadEvents;
        private String workloadEventsInvalid;
        private String workloadRequest;

        public String getWorkloadEvents() {
            return workloadEvents;
        }

        public void setWorkloadEvents(String workloadEvents) {
            this.workloadEvents = workloadEvents;
        }

        public String getWorkloadEventsInvalid() {
            return workloadEventsInvalid;
        }

        public void setWorkloadEventsInvalid(String workloadEventsInvalid) {
            this.workloadEventsInvalid = workloadEventsInvalid;
        }

        public String getWorkloadRequest() {
            return workloadRequest;
        }

        public void setWorkloadRequest(String workloadRequest) {
            this.workloadRequest = workloadRequest;
        }
    }

    public static class Listener {
        private String concurrency = "3-10";

        public String getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(String concurrency) {
            this.concurrency = concurrency;
        }
    }
}
