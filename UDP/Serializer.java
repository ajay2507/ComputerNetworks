//package UDP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
    
	
	// convert the input object to byte array stream of bytes
	public static byte[] covertToBytes(Object object) throws IOException {
		//System.out.println("converting object to bytes");
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bs);
		os.writeObject(object);
		//System.out.println("output byte array: "+bs.toByteArray());
		return bs.toByteArray();
	}
    
	// convert the input bytes to object
	public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
		ObjectInputStream os = new ObjectInputStream(bs);
		return os.readObject();
	}
}
