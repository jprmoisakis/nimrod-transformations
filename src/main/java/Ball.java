public class Ball {

    private long offsetCommitPeriodMs;
    private int maxRetries;
    private int maxUncommittedOffsets;
    private  String firstPollOffsetStrategy;
    private  String kafkaSpoutStreams;
    private String tuplesBuilder;
    private String retryService;
    private String kafkaProps;
    private String keyDeserializer;
    private String valueDeserializer;
    private String subscribedTopics;
    private String  topicWildcardPattern;
    private String  subscription;
    private String  translator;
    private String pollTimeoutMs;
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
        this.subscription = "";
        this.translator = "";
        this.pollTimeoutMs = "";
        this.finalString = finalString();
    }

    public String getFinalString() {
        return finalString;
    }

    public String getKeyDeserializer() {
        return keyDeserializer;
    }

    public String getValueDeserializer() {
        return valueDeserializer;
    }

    public String getSubscribedTopics() {
        return subscribedTopics;

    }

    public String getTopicWildcardPattern() {
        return topicWildcardPattern;
    }

    public String finalString() {
        String returnString = "KafkaSpoutConfig{" +
                "kafkaProps=" + kafkaProps +
                ", key=" + getKeyDeserializer() +
                ", value=" + getValueDeserializer() +
                ", pollTimeoutMs=" + pollTimeoutMs +
                ", offsetCommitPeriodMs=" + offsetCommitPeriodMs +
                ", maxUncommittedOffsets=" + maxUncommittedOffsets +
                ", firstPollOffsetStrategy=" + firstPollOffsetStrategy +
                ", subscription=" + subscription +
                ", translator=" + translator +
                ", retryService=" + retryService +
                '}';
        return returnString;
    }

}

