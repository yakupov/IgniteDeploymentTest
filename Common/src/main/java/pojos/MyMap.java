package pojos;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyMap implements Map<String, Object>, Externalizable, KryoSerializable {
    private final Map<String, Object> _map = new HashMap<>(); //TODO: test THashMap

    public MyMap() {
    }

    private <T> T get(String key) {
        //noinspection unchecked
        return (T) _map.get(key);
    }

    @Override
    public void write(Kryo kryo, Output out) {
        final byte bytes[] = toBytes();
        out.writeInt(bytes.length, true);
        out.write(bytes);
    }

    @Override
    public void read(Kryo kryo, Input in) {
        final int len = in.readInt(true);
        final byte buf[] = new byte[len];
        in.readBytes(buf);
        fromBytes(buf);
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        final byte bytes[] = toBytes();
        out.writeByte(1);
        out.writeInt(bytes.length);
        out.write(bytes);
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final byte ver = in.readByte(); // Serialization version

        final int len = in.readInt();
        final byte buf[] = new byte[len];
        in.readFully(buf);
        fromBytes(buf);
    }

    private List<String> keySetFilterNotEmpty() {
        final Set<String> keySet = keySet();
        final List<String> filledKeys = new ArrayList<>(keySet.size());
        for (final String key : keySet) {
            if (key != null) {
                filledKeys.add(key);
            }
        }
        return filledKeys;
    }

    private byte[] toBytes() {
        final byte[] bytes;
        try (final ByteBufferOutput output = new ByteBufferOutput(32 * 1024, 128 * 1024)) {
            try {
                final Kryo kryo = new Kryo();
                final List<String> filledKeys = keySetFilterNotEmpty();
                output.writeInt(filledKeys.size(), true);
                for (final String key : filledKeys) {
                    output.writeString(key);
                    final Object val = get(key);
                    kryo.writeClassAndObject(output, val);
                }
                bytes = output.toBytes();
            } finally {
                output.release();
            }
        }
        return bytes;
    }

    private void fromBytes(final byte[] buf) {
        if (buf == null || buf.length < 1) {
            return;
        }
        try (final ByteBufferInput in = new ByteBufferInput(buf)) {
            try {
                final Kryo kryo = new Kryo();
                final int fieldsCount = in.readInt(true);
                for (int i = 0; i < fieldsCount; i++) {
                    final String key = in.readString();
                    final Object val = kryo.readClassAndObject(in);
                    put(key, val);
                }
            } finally {
                in.release();
            }
        }
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return _map.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return _map.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return _map.entrySet();
    }


    @Override
    public int size() {
        return this._map.size();
    }

    @Override
    public boolean isEmpty() {
        return this._map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this._map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this._map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this._map.get(key);
    }

    @Override
    public Object remove(Object key) {
        return this._map.remove(key);
    }

    @Override
    public void putAll(@NotNull final Map<? extends String, ?> map) {
        for (final Entry<? extends String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        final Set<String> keySetCopy = new HashSet<>(keySet());
        keySetCopy.forEach(this::remove);
    }

    @Override
    public Object put(final String key, Object value) {
        return _map.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyMap myMap = (MyMap) o;

        return _map != null ? _map.equals(myMap._map) : myMap._map == null;
    }

    @Override
    public int hashCode() {
        return _map != null ? _map.hashCode() : 0;
    }
}
