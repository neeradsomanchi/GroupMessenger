package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by Molu on 5/3/17.
 */

public class MessageContainer implements Comparable<MessageContainer> {

    private float priority;
    private String msg;
    private boolean deliverable = false;
    private String msgSource;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    private int msgId;

    public String getMsgSource() {return msgSource;}

    public void setMsgSource(String actualSource){
        this.msgSource = actualSource;
    }


    public float getPriority() {
        return priority;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }


    public String getMsg() {
        return msg;
    }


    public boolean isDeliverable() {
        return deliverable;
    }

    public void setDeliverable(boolean deliverable) {
        this.deliverable = deliverable;
    }

    public int compareTo(MessageContainer targetObject)
    {
        float prioX = this.getPriority();
        float prioY = targetObject.getPriority();


        if(prioX < prioY)
            return -1;
        else
            return 1;
    }

    public boolean equals (Object targetObject)
    {
        String xMsg = this.getMsg();
        String xSource = this.getMsgSource();
        Integer xMsgId = this.getMsgId();

        MessageContainer msgContainerObj = (MessageContainer) targetObject;

        String yMsg = msgContainerObj.getMsg();
        String ySource = msgContainerObj.getMsgSource();
        Integer yMsgId = msgContainerObj.getMsgId();

        return xMsg.equals(yMsg) && xSource.equals(ySource) && xMsgId.equals(yMsgId);
    }


    public MessageContainer(String msg)
    {
        this.msg = msg;
    }
}
