
public class Ball {
    private final long offsetCommitPeriodMs;
    private final int maxRetries;
    private final int maxUncommittedOffsets;
    private final  String firstPollOffsetStrategy;
    private final  String kafkaSpoutStreams;
    private final String tuplesBuilder;
    private final String retryService;
    private final String kafkaProps;
    private final String keyDeserializer;
    private final String valueDeserializer;
    private final String subscribedTopics;
    private final String  topicWildcardPattern;
    private String finalString;

    public Ball(long offsetCommitPeriodMs, int maxRetries, int maxUncommittedOffsets, String firstPollOffsetStrategy, String kafkaSpoutStreams, String tuplesBuilder, String retryService, String kafkaProps, String keyDeserializer, String valueDeserializer, String subscribedTopics, String topicWildcardPattern) {
        this.offsetCommitPeriodMs = offsetCommitPeriodMs;
        this.maxRetries = maxRetries;
        this.maxUncommittedOffsets = maxUncommittedOffsets;
        this.firstPollOffsetStrategy = firstPollOffsetStrategy;
        this.kafkaSpoutStreams = kafkaSpoutStreams;
        this.tuplesBuilder = tuplesBuilder;
        this.retryService = retryService;
        this.kafkaProps = kafkaProps;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.subscribedTopics = subscribedTopics;
        this.topicWildcardPattern = topicWildcardPattern;
        this.finalString = finalString();
    }

    private String getFinalString() {
        return finalString;
    }

    private String getSubscribedTopics() {
        return subscribedTopics;
    }

    private  final String getTopicWildcardPattern() {
        return topicWildcardPattern;
    }

    public String finalString() {
        String returnString = "KafkaSpoutConfig{" +
                "kafkaProps=" + kafkaProps +
                ", keyDeserializer=" + keyDeserializer +
                ", valueDeserializer=" + valueDeserializer +
                ", pollTimeoutMs=" + valueDeserializer +
                ", offsetCommitPeriodMs=" + offsetCommitPeriodMs +
                ", maxRetries=" + maxRetries +
                ", maxUncommittedOffsets=" + maxUncommittedOffsets +
                ", firstPollOffsetStrategy=" + firstPollOffsetStrategy +
                ", kafkaSpoutStreams=" + kafkaSpoutStreams +
                ", tuplesBuilder=" + tuplesBuilder +
                ", retryService=" + retryService +
                ", topics=" + getSubscribedTopics() +
                ", topicWildcardPattern=" + getTopicWildcardPattern() +
                '}';
        return returnString;
    }

}
