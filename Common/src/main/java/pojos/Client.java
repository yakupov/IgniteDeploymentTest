package pojos;

public class Client extends MyMap {
    private static final String ID = "id";
    private static final String PAYLOAD = "pld";

    public String getId() {
        return (String) get(ID);
    }

    public void setId(String id) {
        put(ID, id);
    }

    public String getPayload() {
        return (String) get(PAYLOAD);
    }

    public void setPayload(String payload) {
        put(PAYLOAD, payload);
    }
}
