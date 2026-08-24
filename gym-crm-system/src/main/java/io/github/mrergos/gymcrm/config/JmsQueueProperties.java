package io.github.mrergos.gymcrm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jms")
public class JmsQueueProperties {

    private Queues queues = new Queues();
    private long replyTimeoutMs = 5000L;

    public Queues getQueues() {
        return queues;
    }

    public void setQueues(Queues queues) {
        this.queues = queues;
    }

    public long getReplyTimeoutMs() {
        return replyTimeoutMs;
    }

    public void setReplyTimeoutMs(long replyTimeoutMs) {
        this.replyTimeoutMs = replyTimeoutMs;
    }

    public static class Queues {
        private String workloadEvents;
        private String workloadEventsInvalid;
        private String workloadRequest;
        private String workloadReplyPrefix;

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

        public String getWorkloadReplyPrefix() {
            return workloadReplyPrefix;
        }

        public void setWorkloadReplyPrefix(String workloadReplyPrefix) {
            this.workloadReplyPrefix = workloadReplyPrefix;
        }
    }
}