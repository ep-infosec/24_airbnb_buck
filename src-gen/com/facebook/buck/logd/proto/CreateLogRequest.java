// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/com/facebook/buck/logd/proto/logdservice.proto

package com.facebook.buck.logd.proto;

/**
 * <pre>
 * Request message for [LogdService.CreateLogFile] to request a log file be created
 * in file-system and/or storage
 * </pre>
 *
 * Protobuf type {@code logd.v1.CreateLogRequest}
 */
@javax.annotation.Generated(value="protoc", comments="annotations:CreateLogRequest.java.pb.meta")
public  final class CreateLogRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:logd.v1.CreateLogRequest)
    CreateLogRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use CreateLogRequest.newBuilder() to construct.
  private CreateLogRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private CreateLogRequest() {
    logFilePath_ = "";
    logType_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private CreateLogRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            logFilePath_ = s;
            break;
          }
          case 16: {
            int rawValue = input.readEnum();

            logType_ = rawValue;
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.facebook.buck.logd.proto.LogdServiceOuterFile.internal_static_logd_v1_CreateLogRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.facebook.buck.logd.proto.LogdServiceOuterFile.internal_static_logd_v1_CreateLogRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.facebook.buck.logd.proto.CreateLogRequest.class, com.facebook.buck.logd.proto.CreateLogRequest.Builder.class);
  }

  public static final int LOGFILEPATH_FIELD_NUMBER = 1;
  private volatile java.lang.Object logFilePath_;
  /**
   * <pre>
   * path to which log will be streamed by logD
   * </pre>
   *
   * <code>string logFilePath = 1;</code>
   */
  public java.lang.String getLogFilePath() {
    java.lang.Object ref = logFilePath_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      logFilePath_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * path to which log will be streamed by logD
   * </pre>
   *
   * <code>string logFilePath = 1;</code>
   */
  public com.google.protobuf.ByteString
      getLogFilePathBytes() {
    java.lang.Object ref = logFilePath_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      logFilePath_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LOGTYPE_FIELD_NUMBER = 2;
  private int logType_;
  /**
   * <pre>
   * enum specifying existing types of log
   * </pre>
   *
   * <code>.logd.v1.LogType logType = 2;</code>
   */
  public int getLogTypeValue() {
    return logType_;
  }
  /**
   * <pre>
   * enum specifying existing types of log
   * </pre>
   *
   * <code>.logd.v1.LogType logType = 2;</code>
   */
  public com.facebook.buck.logd.proto.LogType getLogType() {
    @SuppressWarnings("deprecation")
    com.facebook.buck.logd.proto.LogType result = com.facebook.buck.logd.proto.LogType.valueOf(logType_);
    return result == null ? com.facebook.buck.logd.proto.LogType.UNRECOGNIZED : result;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getLogFilePathBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, logFilePath_);
    }
    if (logType_ != com.facebook.buck.logd.proto.LogType.BUCK_LOG.getNumber()) {
      output.writeEnum(2, logType_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getLogFilePathBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, logFilePath_);
    }
    if (logType_ != com.facebook.buck.logd.proto.LogType.BUCK_LOG.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, logType_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.facebook.buck.logd.proto.CreateLogRequest)) {
      return super.equals(obj);
    }
    com.facebook.buck.logd.proto.CreateLogRequest other = (com.facebook.buck.logd.proto.CreateLogRequest) obj;

    if (!getLogFilePath()
        .equals(other.getLogFilePath())) return false;
    if (logType_ != other.logType_) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + LOGFILEPATH_FIELD_NUMBER;
    hash = (53 * hash) + getLogFilePath().hashCode();
    hash = (37 * hash) + LOGTYPE_FIELD_NUMBER;
    hash = (53 * hash) + logType_;
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.facebook.buck.logd.proto.CreateLogRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.facebook.buck.logd.proto.CreateLogRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * Request message for [LogdService.CreateLogFile] to request a log file be created
   * in file-system and/or storage
   * </pre>
   *
   * Protobuf type {@code logd.v1.CreateLogRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:logd.v1.CreateLogRequest)
      com.facebook.buck.logd.proto.CreateLogRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.facebook.buck.logd.proto.LogdServiceOuterFile.internal_static_logd_v1_CreateLogRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.facebook.buck.logd.proto.LogdServiceOuterFile.internal_static_logd_v1_CreateLogRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.facebook.buck.logd.proto.CreateLogRequest.class, com.facebook.buck.logd.proto.CreateLogRequest.Builder.class);
    }

    // Construct using com.facebook.buck.logd.proto.CreateLogRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      logFilePath_ = "";

      logType_ = 0;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.facebook.buck.logd.proto.LogdServiceOuterFile.internal_static_logd_v1_CreateLogRequest_descriptor;
    }

    @java.lang.Override
    public com.facebook.buck.logd.proto.CreateLogRequest getDefaultInstanceForType() {
      return com.facebook.buck.logd.proto.CreateLogRequest.getDefaultInstance();
    }

    @java.lang.Override
    public com.facebook.buck.logd.proto.CreateLogRequest build() {
      com.facebook.buck.logd.proto.CreateLogRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.facebook.buck.logd.proto.CreateLogRequest buildPartial() {
      com.facebook.buck.logd.proto.CreateLogRequest result = new com.facebook.buck.logd.proto.CreateLogRequest(this);
      result.logFilePath_ = logFilePath_;
      result.logType_ = logType_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.facebook.buck.logd.proto.CreateLogRequest) {
        return mergeFrom((com.facebook.buck.logd.proto.CreateLogRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.facebook.buck.logd.proto.CreateLogRequest other) {
      if (other == com.facebook.buck.logd.proto.CreateLogRequest.getDefaultInstance()) return this;
      if (!other.getLogFilePath().isEmpty()) {
        logFilePath_ = other.logFilePath_;
        onChanged();
      }
      if (other.logType_ != 0) {
        setLogTypeValue(other.getLogTypeValue());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.facebook.buck.logd.proto.CreateLogRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.facebook.buck.logd.proto.CreateLogRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object logFilePath_ = "";
    /**
     * <pre>
     * path to which log will be streamed by logD
     * </pre>
     *
     * <code>string logFilePath = 1;</code>
     */
    public java.lang.String getLogFilePath() {
      java.lang.Object ref = logFilePath_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        logFilePath_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * path to which log will be streamed by logD
     * </pre>
     *
     * <code>string logFilePath = 1;</code>
     */
    public com.google.protobuf.ByteString
        getLogFilePathBytes() {
      java.lang.Object ref = logFilePath_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        logFilePath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * path to which log will be streamed by logD
     * </pre>
     *
     * <code>string logFilePath = 1;</code>
     */
    public Builder setLogFilePath(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      logFilePath_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * path to which log will be streamed by logD
     * </pre>
     *
     * <code>string logFilePath = 1;</code>
     */
    public Builder clearLogFilePath() {
      
      logFilePath_ = getDefaultInstance().getLogFilePath();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * path to which log will be streamed by logD
     * </pre>
     *
     * <code>string logFilePath = 1;</code>
     */
    public Builder setLogFilePathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      logFilePath_ = value;
      onChanged();
      return this;
    }

    private int logType_ = 0;
    /**
     * <pre>
     * enum specifying existing types of log
     * </pre>
     *
     * <code>.logd.v1.LogType logType = 2;</code>
     */
    public int getLogTypeValue() {
      return logType_;
    }
    /**
     * <pre>
     * enum specifying existing types of log
     * </pre>
     *
     * <code>.logd.v1.LogType logType = 2;</code>
     */
    public Builder setLogTypeValue(int value) {
      logType_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * enum specifying existing types of log
     * </pre>
     *
     * <code>.logd.v1.LogType logType = 2;</code>
     */
    public com.facebook.buck.logd.proto.LogType getLogType() {
      @SuppressWarnings("deprecation")
      com.facebook.buck.logd.proto.LogType result = com.facebook.buck.logd.proto.LogType.valueOf(logType_);
      return result == null ? com.facebook.buck.logd.proto.LogType.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * enum specifying existing types of log
     * </pre>
     *
     * <code>.logd.v1.LogType logType = 2;</code>
     */
    public Builder setLogType(com.facebook.buck.logd.proto.LogType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      logType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * enum specifying existing types of log
     * </pre>
     *
     * <code>.logd.v1.LogType logType = 2;</code>
     */
    public Builder clearLogType() {
      
      logType_ = 0;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:logd.v1.CreateLogRequest)
  }

  // @@protoc_insertion_point(class_scope:logd.v1.CreateLogRequest)
  private static final com.facebook.buck.logd.proto.CreateLogRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.facebook.buck.logd.proto.CreateLogRequest();
  }

  public static com.facebook.buck.logd.proto.CreateLogRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<CreateLogRequest>
      PARSER = new com.google.protobuf.AbstractParser<CreateLogRequest>() {
    @java.lang.Override
    public CreateLogRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new CreateLogRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<CreateLogRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<CreateLogRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.facebook.buck.logd.proto.CreateLogRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

