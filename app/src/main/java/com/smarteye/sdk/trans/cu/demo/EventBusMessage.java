package com.smarteye.sdk.trans.cu.demo;

public class EventBusMessage {
    public static final int EVENTBUS_GET_PU_LIST_RESPONSE = 101;
    public static final int EVENTBUS_GET_UA_LIST_RESPONSE = 102;
    public static final int EVENTBUS_ON_DIALOG_EVENT = 103;

    private int messageType;// 消息类型,用于区分不同消息
    private Object[] objectArray;// 负载

    public EventBusMessage(int messageType) {
        this.messageType = messageType;
    }

    public EventBusMessage(int messageType, Object[] objArr) {
        this.messageType = messageType;
        this.objectArray = objArr;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public Object[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(Object[] objectArray) {
        this.objectArray = objectArray;
    }
}
