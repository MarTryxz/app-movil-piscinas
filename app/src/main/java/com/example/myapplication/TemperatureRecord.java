package com.example.myapplication;

public class TemperatureRecord {
    private String id; // Firebase Push ID
    private float tempAgua;
    private float tempAire;
    private float humedadAire;

    public TemperatureRecord() {
        // Default constructor required for calls to
        // DataSnapshot.getValue(TemperatureRecord.class)
    }

    public TemperatureRecord(String id, float tempAgua, float tempAire, float humedadAire) {
        this.id = id;
        this.tempAgua = tempAgua;
        this.tempAire = tempAire;
        this.humedadAire = humedadAire;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getTempAgua() {
        return tempAgua;
    }

    public void setTempAgua(float tempAgua) {
        this.tempAgua = tempAgua;
    }

    public float getTempAire() {
        return tempAire;
    }

    public void setTempAire(float tempAire) {
        this.tempAire = tempAire;
    }

    public float getHumedadAire() {
        return humedadAire;
    }

    public void setHumedadAire(float humedadAire) {
        this.humedadAire = humedadAire;
    }

    public long getTimestamp() {
        if (id == null || id.length() < 8)
            return 0;
        // Firebase Push IDs' first 8 characters represent the timestamp
        // See:
        // https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
        String timestampStr = id.substring(0, 8);
        // This is a simplified extraction. In a real push ID, it's base64-like but
        // custom.
        // However, for display purposes without a complex decoder, we might need a
        // proper decoder
        // or just rely on the fact that they are sorted chronologically.
        // Let's try to decode it properly if possible, or just return 0 and handle date
        // formatting in Adapter if we can't easily decode.
        // Actually, for this task, let's just store the ID. The user asked to "mostrar
        // el historial".
        // I'll add a helper to get a readable date if possible, but standard decoding
        // is complex.
        // Let's stick to just returning the ID for now or maybe the order is enough?
        // Wait, the user wants to see the history. Usually implies a date.
        // I will implement a basic PushID timestamp decoder.
        return getTimestampFromPushId(id);
    }

    private long getTimestampFromPushId(String pushId) {
        // Modeled after Firebase's own generator logic
        final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
        long time = 0;
        for (int i = 0; i < 8; i++) {
            char c = pushId.charAt(i);
            time = time * 64 + PUSH_CHARS.indexOf(c);
        }
        return time;
    }
}
