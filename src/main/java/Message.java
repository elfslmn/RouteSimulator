import java.util.Hashtable;

/**
 * Created by esalman17 on 11.12.2018.
 */

public class Message {
    public int senderID, receiverID;
    Hashtable<Integer, Integer> content;

    public Message(int senderID, int receiverID, Hashtable<Integer, Integer> content) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
    }
}
