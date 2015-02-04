package com.anyvalue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

@SuppressWarnings({"unchecked"})

class DType {
	
	public final static int Integer1 = 0;
	public final static int Integer2 = 1;
	public final static int Integer4 = 2;
	public final static int Integer8 = 3;
	public final static int Integer = 3;
	public final static int String1 = 4;
	public final static int String2 = 5;
	public final static int String4 = 6;
	public final static int String = 6;
	public final static int Vector = 7;
	public final static int Map = 8;
	public final static int Ext = 9;
	public final static int Float = 10;
	public final static int Bool = 11;
	public final static int Null = 12;
	public final static int SInteger1 = 13;
	public final static int SInteger2 = 14;
	public final static int SInteger4 = 15;
	public final static int SInteger8 = 16;
	public final static int SInteger = 16;
}

class CRef
{
	public int pos;
	public int size;
}

public class CAnyValue {

	private Object data;
	private int type;
	private int subtype;
	private static char[] hex = "0123456789ABCDEF".toCharArray();

	public CAnyValue() {
		type = -1;
		subtype = -1;
		data = null;
	}

	public CAnyValue(Object o) {
		if (o instanceof Boolean) {
			type = DType.Bool;
			subtype = DType.Bool;
			data = o;
		} else if (o instanceof Byte) {
			subtype = (Byte) o < 0 ? DType.SInteger1 : DType.Integer1;
			type = (Byte) o < 0 ? DType.SInteger : DType.Integer;
			data = o;
		} else if (o instanceof Short) {
			subtype = (Short) o < 0 ? DType.SInteger2 : DType.Integer2;
			type = (Short) o < 0 ? DType.SInteger : DType.Integer;
			data = o;
		} else if (o instanceof Integer) {
			subtype = (Integer) o < 0 ? DType.SInteger4 : DType.Integer4;
			type = (Integer) o < 0 ? DType.SInteger : DType.Integer;
			data = o;
		} else if (o instanceof Long) {
			subtype = (Long) o < 0 ? DType.SInteger8 : DType.Integer8;
			type = (Long) o < 0 ? DType.SInteger : DType.Integer;
			data = o;
		} else if (o instanceof Double) {
			type = DType.Float;
			subtype = DType.Float;
			data = o;
		} else if (o instanceof Float) {
			type = DType.Float;
			data = o;
			subtype = DType.Float;
		} else if (o == null) {
			type = DType.Null;
			subtype = DType.Null;
			data = o;
		} else if (o instanceof String) {
			String str = (String) o;
			if (str.length() < 0xFF) {
				type = DType.String;
				subtype = DType.String1;
			} else if (str.length() < 0xFFFF) {
				subtype = DType.String2;
				type = DType.String;

			} else {
				subtype = DType.String4;
				type = DType.String;

			}
			data = o;
		}
		else 
		{
			putObject(o);
		}
	}
	
	private void putObject(Object obj)
	{
		try
		{
			initAsMap();
			Field []fields = obj.getClass().getDeclaredFields();
			
			for(int i=0;i<fields.length;i++)
			{
				fields[i].setAccessible(true);
				String fieldType = fields[i].getType().getName();
				if(fieldType == "long" ||fieldType == "int" ||fieldType == "short" || fieldType == "java.lang.String" ||
						fieldType == "byte" ||fieldType =="float" ||fieldType=="double")
				{
					((Map<String, CAnyValue>) data).put(fields[i].getName(), new CAnyValue(fields[i].get(obj)));
				}
			}
		}
		catch(Exception e)
		{
			throw new Error(e.getMessage());
		}
	}

	public void initAsMap() {
		if (type == -1 || data == null) {
			type = DType.Map;
			subtype = DType.Map;
			data = new LinkedHashMap<String, CAnyValue>();
		}
	}

	public void initAsArray() {
		if (type == -1 || data == null) {
			type = DType.Vector;
			subtype = DType.Vector;
			data = new ArrayList<CAnyValue>();
		}

	}

	private void decode(HeapBuffer bytedata) {

		int valuetype = bytedata.get();
		switch (valuetype) {
			case DType.Bool:
				decode_bool(bytedata);
				break;
			case DType.Null:
				subtype = DType.Null;
				type = DType.Null;
				break;
			case DType.Float:
				decode_float(bytedata);
				break;
			case DType.Integer1:
				decode_integer1(bytedata);
				break;
			case DType.Integer2:
				decode_integer2(bytedata);
				break;
			case DType.Integer4:
				decode_integer4(bytedata);
				break;
			case DType.Integer8:
				decode_integer8(bytedata);
				break;
			case DType.SInteger1:
				decode_integer1(bytedata);
				break;
			case DType.SInteger2:
				decode_integer2(bytedata);
				break;
			case DType.SInteger4:
				decode_integer4(bytedata);
				break;
			case DType.SInteger8:
				decode_integer8(bytedata);
				break;
			case DType.String1:
				decode_string1(bytedata);
				break;
			case DType.String2:
				decode_string2(bytedata);
				break;
			case DType.String4:
				decode_string4(bytedata);
				break;
			case DType.Vector:
				decode_vector(bytedata);
				break;
			case DType.Map:
				decode_map(bytedata);
				break;
			default:
				break;
		}

	}

	public void decode(byte [] data)
	{
		HeapBuffer bytedata = HeapBuffer.wrap(data);
		decode(bytedata);
	}
	
	public void decode(byte [] data,int offset,int length)
	{
		HeapBuffer bytedata = HeapBuffer.wrap(data, offset, length);
		decode(bytedata);
	}
		
	private void decode_bool(HeapBuffer bytedata) {

		byte v =  bytedata.get();
		type = DType.Bool;
		subtype = DType.Bool;
		if (v == 1) {
			data = true;
		} else {
			data = false;
		}
	}

	private void decode_float(HeapBuffer bytedata) {

		type = DType.Float;
		subtype = DType.Float;
		data = (Double) bytedata.getDouble();
	}

	private void decode_integer1(HeapBuffer bytedata) {

		type = DType.Integer;
		subtype = DType.Integer1;
		data = (Byte) bytedata.get();
	}

	private void decode_integer2(HeapBuffer bytedata) {

		type = DType.Integer;
		subtype = DType.Integer2;
		data = (Short) bytedata.getShort();// &0xFFFF;

	}

	private void decode_integer4(HeapBuffer bytedata) {

		type = DType.Integer;
		subtype = DType.Integer4;
		data = (Integer) bytedata.getInt();// &0xFFFFFFFF;
	}

	private void decode_integer8(HeapBuffer bytedata) {

		type = DType.Integer;
		subtype = DType.Integer8;
		data = (Long) bytedata.getLong();
	}

	private static long getUnsignedNum(Object o) {
		if (o instanceof Byte) {
			return (long) (((Byte) o).byteValue() & 0xFF);
		}
		if (o instanceof Short) {
			return (long) (((Short) o).shortValue() & 0xFFFF);
		}
		if (o instanceof Integer) {
			return (long) (((Integer) o).intValue() & 0xFFFFFFFF);
		}
		if (o instanceof Long) {
			return ((Long)o).longValue();
		}
		return 0;
	}

	private void decode_string1(HeapBuffer bytedata) {
		type = DType.String;
		subtype = DType.String1;
		int length = bytedata.get() & 0xFF;
		byte strdata[] = new byte[length];
		bytedata.get(strdata, 0, length);
		data = new String(strdata);

	}

	private void decode_string2(HeapBuffer bytedata) {
		type = DType.String;
		subtype = DType.String2;
		int length = (int) bytedata.getShort() & 0xFFFF;
		byte strdata[] = new byte[length];
		bytedata.get(strdata, 0, length);
		data = new String(strdata);

	}

	private void decode_string4(HeapBuffer bytedata) {
		type = DType.String;
		subtype = DType.String4;
		int length = bytedata.getInt();
		byte strdata[] = new byte[length];
		bytedata.get(strdata, 0, length);
		data = new String(strdata);
	}

	private void decode_vector(HeapBuffer bytedata) {
		initAsArray();
		type = DType.Vector;
		subtype = DType.Vector;
		long length = bytedata.getInt() & 0xFFFFFFFF;
		for (int i = 0; i < length; ++i) {
			CAnyValue value = new CAnyValue();
			value.decode(bytedata);
			((List<CAnyValue>) data).add(value);
		}

	}

	private void decode_map(HeapBuffer bytedata) {
		initAsMap();
		type = DType.Map;
		subtype = DType.Map;
		long length = bytedata.getInt() & 0xFFFFFFFF;
		for (int i = 0; i < length; ++i) {
			int strlength = bytedata.get() & 0xff;
			byte strdata[] = new byte[strlength];
			bytedata.get(strdata, 0, strlength);
			CAnyValue value = new CAnyValue();
			value.decode(bytedata);
			((Map<String, CAnyValue>) data).put(new String(strdata), value);
		}
	}

	public byte [] encode()
	{
		HeapBuffer buffer = HeapBuffer.allocate();
		encode(buffer);
		byte [] data = new byte[buffer.position()];
		System.arraycopy(buffer.array(), 0, data, 0, buffer.position());
		return data;
	}
	
	
	private void encode(HeapBuffer buffer) {
		if (type < -1) {
			return;
		}
		switch (type) {
			case DType.Bool:
				encode_bool(buffer);
				break;
			case DType.Integer:
				encode_integer(buffer);
				break;
			case DType.SInteger:
				encode_sinteger(buffer);
				break;
			case DType.Float:
				encode_float(buffer);
				break;
			case DType.String:
				encode_string(buffer);
				break;
			case DType.Vector:
				encode_vector(buffer);
				break;
			case DType.Map:
				encode_map(buffer);
				break;
			case DType.Null:
			case -1:
				encode_null(buffer);
				break;
			default:
				break;
		}
	}

	private void encode_null(HeapBuffer buffer) {
		buffer.put((byte) DType.Null);
	}

	private void encode_bool(HeapBuffer buffer) {
		buffer.put((byte) DType.Bool);
		if ((Boolean) data == true) {
			buffer.put((byte) 1);
		} else {
			buffer.put((byte) 0);
		}
	}

	private void encode_float(HeapBuffer buffer) {
		buffer.put((byte) DType.Float);
		double flValue = (Double) data;
		buffer.putDouble(flValue);
	}

	private void encode_integer(HeapBuffer buffer) {
		long num = getUnsignedNum(data);
		if (num <= 0xFF) {
			buffer.put((byte) DType.Integer1);
			buffer.put((byte) num);
		} else if (num <= 0xFFFF) {
			buffer.put((byte) DType.Integer2);
			buffer.putShort((short) num);
		} else if (num <= 0xFFFFFFFF) {
			buffer.put((byte) DType.Integer4);
			buffer.putInt((int) num);
		} else {
			buffer.put((byte) DType.Integer8);
			buffer.putLong(num);
		}
	}

	private void encode_sinteger(HeapBuffer buffer) {
		long num = getUnsignedNum(data);
		if (num > -129) {
			buffer.put((byte) DType.SInteger1);
			buffer.put((byte) num);
		} else if (num > -32769) {
			buffer.put((byte) DType.SInteger2);
			buffer.putShort((short) num);
		} else if (num > -2147483649L) {
			buffer.put((byte) DType.SInteger4);
			buffer.putInt((int) num);
		} else {
			buffer.put((byte) DType.SInteger8);
			buffer.putLong(num);
		}
	}

	private void encode_string(HeapBuffer buffer) {
		String str = (String) data;
		byte[] bytes = str.getBytes();
		if (bytes.length <= 0xFF) {
			buffer.put((byte) DType.String1);
			buffer.put((byte) bytes.length);
			buffer.put(bytes);
		} else if (bytes.length <= 0xFFFF) {
			buffer.put((byte) DType.String2);
			buffer.putShort((short) bytes.length);
			buffer.put(bytes);
		} else {
			buffer.put((byte) DType.String4);
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		}
	}

	private void encode_vector(HeapBuffer buffer) {
		buffer.put((byte) DType.Vector);
		int size = ((List<CAnyValue>) data).size();
		buffer.putInt(size);
		Iterator iterator = ((List<CAnyValue>) data).iterator();
		while (iterator.hasNext()) {
			((CAnyValue) iterator.next()).encode(buffer);
		}
	}

	public String encodeJSON()
	{
		StringBuffer sBuf = new StringBuffer();
		encodeJSON(sBuf);
		return sBuf.toString();
	}
	
	private void encodeJSON(StringBuffer sBuf) {

		switch (type) {
			case DType.SInteger:
			case DType.Integer:
			case DType.Float:
			case DType.Bool: {
				sBuf.append(data);
			}
			break;
			case DType.Null:
			case -1: {
				sBuf.append("null");
			}
			break;
			case DType.String: {
				sBuf.append("\"");
				dumpString(sBuf,(String)data);
				sBuf.append("\"");
			}
			break;
			case DType.Vector: {
				boolean bFirst = true;
				sBuf.append("[");
				Iterator iterator = ((List<CAnyValue>) data).iterator();
				while (iterator.hasNext()) {
					if (!bFirst) {
						sBuf.append(",");
					} else {
						bFirst = false;
					}
					((CAnyValue) iterator.next()).encodeJSON(sBuf);
				}
				sBuf.append("]");
			}
			break;
			case DType.Map: {
				sBuf.append("{");
				boolean bFirst = true;
				Iterator iterator = ((Map<String, CAnyValue>) data).entrySet().iterator();
				while (iterator.hasNext()) {
					if (bFirst) {
						sBuf.append("\"");
						bFirst = false;
					} else {
						sBuf.append(",\"");
					}

					Map.Entry<String, CAnyValue> entry = (Map.Entry<String, CAnyValue>) iterator.next();
					String key = entry.getKey();
					sBuf.append(key);
					sBuf.append("\":");
					CAnyValue value = (CAnyValue) entry.getValue();
					value.encodeJSON(sBuf);
				}
				sBuf.append("}");
			}
			break;
			default: {
				sBuf.append("\"\"");
			}
			break;
		}
	}

	private static void dumpString(StringBuffer sb,String s) {

    	int len = s.length();
		for(int i=0;i<len;i++){
			char ch=s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				int n = (int)ch;
				if( n < 0x20 || (n >= 0x7F && n <= 0x9F) || (n >= 0x2000 && n <= 0x20FF)){
					sb.append("\\u");
                    for (int j = 0; j < 4; ++j) {
                        int digit = (n & 0xf000) >> 12;
                        sb.append(hex[digit]);
                        n <<= 4;
                    }
				}
				else{
					sb.append(ch);
				}
			}
		}
	}
	
	public void decodeJSON(String sBuf)
	{
		CRef ref  = new CRef();
		ref.pos = 0;
		ref.size = sBuf.length();
		skipSpaces(ref, sBuf);
		char v = sBuf.charAt(ref.pos);
		if( v == '{')
		{
			ref.pos++;
			readObj(ref,this,sBuf);
		}
		else if( v == '[')
		{
			ref.pos++;
			readArray(ref, this, sBuf);
		}
		else if( v == '"')
		{
			StringBuffer sValue = new StringBuffer();
			ref.pos++;
			readString(ref, sValue, sBuf);
			ref.pos++;
			type = DType.String;
			data = sValue.toString();
		}
		else if( v == 't')
		{
			if(ref.pos+4 >= sBuf.length())
			{
				throw new Error("not a bool value");
			}
			String sValue = sBuf.substring(ref.pos, ref.pos+4);
			if(sValue.equals("true") == false)
			{
				throw new Error("not a bool value");
			}
			type = DType.Bool;
			data = true;
			ref.pos+=4;
		}
		else if( v == 'n')
		{
			if(ref.pos+4 >= sBuf.length())
			{
				throw new Error("not a bool value");
			}
			String sValue = sBuf.substring(ref.pos, ref.pos+4);
			if(sValue.equals("null") == false)
			{
				throw new Error("not a null value");
			}
			type = DType.Null;
			data = null;
			ref.pos+=4;
		}
		else if( v == 'f')
		{
			if(ref.pos+5 >= sBuf.length())
			{
				throw new Error("not a bool value");
			}
			String sValue = sBuf.substring(ref.pos, ref.pos+5);
			if(sValue.equals("false") == false)
			{
				throw new Error("not a bool value");
			}
			type = DType.Bool;
			data = false;
			ref.pos+=5;
		}
		else
		{
			StringBuffer sValue = new StringBuffer();
			readNumber(ref,sValue,sBuf);
			if(sValue.indexOf(".") != -1)
			{
				double d = Double.parseDouble(sValue.toString());
				type = DType.Float;
				data = d;
			}
			else
			{
				type = DType.Integer;
				long l = Long.parseLong(sValue.toString());
				data = l;
			}
		}
		
	}
	private static void readObj(CRef ref,CAnyValue value,String sBuf)
	{
		value.initAsMap();
		while(ref.pos < sBuf.length())
		{
			skipSpaces(ref, sBuf);
			char v = sBuf.charAt(ref.pos);
			if(v == '}')
			{
				ref.pos++;
				break;
			}
			if(v == '"')
			{
				StringBuffer key = new StringBuffer();
				ref.pos++;
				readString(ref,key,sBuf);
				ref.pos++;
				skipSpaces(ref, sBuf);
				v = sBuf.charAt(ref.pos);
				if( v != ':')
				{
					throw new Error("expect :");
				}
				ref.pos++;
				skipSpaces(ref, sBuf);
				v = sBuf.charAt(ref.pos);
				if(v == '"')
				{
					ref.pos++;
					StringBuffer sValue = new StringBuffer();
					readString(ref,sValue,sBuf);
					ref.pos++;
					value.put(key.toString(), sValue.toString());
				}
				else if( v == '{')
				{
					ref.pos++;
					CAnyValue objValue = new CAnyValue();
					readObj(ref, objValue, sBuf);
					value.put(key.toString(),objValue);
				}
				else if(v == '[')
				{
					ref.pos++;
					CAnyValue arrayValue = new CAnyValue();
					readArray(ref,arrayValue,sBuf);
					value.put(key.toString(),arrayValue);
				}
				else if( v == 't')
				{
					if(ref.pos+4 >= sBuf.length())
					{
						throw new Error("not a bool value");
					}
					String sValue = sBuf.substring(ref.pos, ref.pos+4);
					if(sValue.equals("true") == false)
					{
						throw new Error("not a bool value");
					}
					value.put(key.toString(),true);
					ref.pos+=4;
				}
				else if( v == 'n')
				{
					if(ref.pos+4 >= sBuf.length())
					{
						throw new Error("not a bool value");
					}
					String sValue = sBuf.substring(ref.pos, ref.pos+4);
					if(sValue.equals("null") == false)
					{
						throw new Error("not a null value");
					}
					value.put(key.toString(),null);
					ref.pos+=4;
				}
				else if( v == 'f')
				{
					if(ref.pos+5 >= sBuf.length())
					{
						throw new Error("not a bool value");
					}
					String sValue = sBuf.substring(ref.pos, ref.pos+5);
					if(sValue.equals("false") == false)
					{
						throw new Error("not a bool value");
					}
					value.put(key.toString(),false);
					ref.pos+=5;
				}
				else
				{
					StringBuffer sValue = new StringBuffer();
					readNumber(ref,sValue,sBuf);
					if(sValue.indexOf(".") != -1)
					{
						double d = Double.parseDouble(sValue.toString());
						value.put(key.toString(),d);
					}
					else
					{
						long l = Long.parseLong(sValue.toString());
						value.put(key.toString(),l);
					}
				}
				skipSpaces(ref, sBuf);
				v = sBuf.charAt(ref.pos);
				if(v == '}')
				{
					ref.pos++;
					break;
				}
				else if(v == ',')
				{
					ref.pos++;
				}
				else
				{
					throw new Error("expect ,");
				}
			}
			else
			{
				throw new Error("expect \"");
			}
			
		}
	}
	
	private static void readArray(CRef ref,CAnyValue value,String sBuf)
	{
		value.initAsArray();
		while(ref.pos < sBuf.length())
		{
			skipSpaces(ref, sBuf);
			char v = sBuf.charAt(ref.pos);
			if(v == ']')
			{
				ref.pos++;
				break;
			}
			else if( v == '{')
			{
				ref.pos++;
				CAnyValue objValue = new CAnyValue();
				readObj(ref, objValue, sBuf);
				value.put(objValue);
			}
			else if(v == '[')
			{
				ref.pos++;
				CAnyValue arrayValue = new CAnyValue();
				readArray(ref,arrayValue,sBuf);
				value.put(arrayValue);
			}
			else if( v == '"' )
			{
				StringBuffer sValue = new StringBuffer();
				ref.pos++;
				readString(ref, sValue, sBuf);
				ref.pos++;
				value.put(sValue.toString());
			}
			else if( v == 't')
			{
				if(ref.pos+4 >= sBuf.length())
				{
					throw new Error("not a bool value");
				}
				String sValue = sBuf.substring(ref.pos, ref.pos+4);
				if(sValue.equals("true") == false)
				{
					throw new Error("not a bool value");
				}
				value.put(true);
				ref.pos+=4;
			}
			else if( v == 'n')
			{
				if(ref.pos+4 >= sBuf.length())
				{
					throw new Error("not a bool value");
				}
				String sValue = sBuf.substring(ref.pos, ref.pos+4);
				if(sValue.equals("null") == false)
				{
					throw new Error("not a null value");
				}
				value.put(null);
				ref.pos+=4;
			}
			else if( v == 'f')
			{
				if(ref.pos+5 >= sBuf.length())
				{
					throw new Error("not a bool value");
				}
				String sValue = sBuf.substring(ref.pos, ref.pos+5);
				if(sValue.equals("false") == false)
				{
					throw new Error("not a bool value");
				}
				value.put(false);
				ref.pos+=5;
			}
			else
			{
				StringBuffer sValue = new StringBuffer();
				readNumber(ref,sValue,sBuf);
				if(sValue.indexOf(".") != -1)
				{
					double d = Double.parseDouble(sValue.toString());
					value.put(d);
				}
				else
				{
					long l = Long.parseLong(sValue.toString());
					value.put(l);
				}
			}
			skipSpaces(ref, sBuf);
			v = sBuf.charAt(ref.pos);
			if(v == ']')
			{
				ref.pos++;
				break;
			}
			else if(v == ',')
			{
				ref.pos++;
			}
			else
			{
				throw new Error("expect ,");
			}
		}
	}
	
	private static void readNumber(CRef ref,StringBuffer sValue,String sBuf)
	{
		while(ref.pos < sBuf.length())
		{
			sValue.append(sBuf.charAt(ref.pos));
			ref.pos++;
			char v = sBuf.charAt(ref.pos);
			if(v == ' '||
				v == ','||
				v == '}'||
				v == ']'||
				v == '\r'||
				v == '\n'||
				v == '\t'||
				v == '\b'||
				v == '\f')
			{
				break;
			}
		}
	}
	private static void readString(CRef ref,StringBuffer sValue,String sBuf)
	{
		int flag = 0;
		while(ref.pos < sBuf.length())
		{
			char v1 = sBuf.charAt(ref.pos);
			char v2 = sBuf.charAt(ref.pos+1);
			if(v1 == '\\' && v2 == '"')
			{
				sValue.append("\"");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 't')
			{
				sValue.append("\t");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 'b')
			{
				sValue.append("\b");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 'f')
			{
				sValue.append("\f");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 'n')
			{
				sValue.append("\n");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 'r')
			{
				sValue.append("\r");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == '\\')
			{
				sValue.append("\\");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == '/')
			{
				sValue.append("/");
				ref.pos++;
			}
			else if(v1 == '\\' && v2 == 'u')
			{
				sValue.append((char)Integer.parseInt(sBuf.substring(ref.pos+2, ref.pos+6),16));
				ref.pos+=5;
			}
			else if(v1 == '"')
			{
				flag = 1;
				break;
			}
			else
			{
				sValue.append(v1);
			}
			ref.pos++;
		}
		if(flag == 0)
		{
			throw new Error("read string error");
		}
	}
	private static void skipSpaces(CRef ref,String sBuf)
	{
		while(ref.pos < sBuf.length())
		{
			char v = sBuf.charAt(ref.pos);
			if(v == ' ' || v == '\t' || v == '\r' || v== '\n' )
			{
				ref.pos++;
			}
			else
			{
				break;
			}
		}
	}
	private void encode_map(HeapBuffer buffer) {
		buffer.put((byte) DType.Map);
		int size = ((Map<String, CAnyValue>) data).size();
		buffer.putInt(size);
		Iterator iterator = ((Map<String, CAnyValue>) data).entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, CAnyValue> entry = (Map.Entry<String, CAnyValue>) iterator.next();
			String key = entry.getKey();
			CAnyValue value = (CAnyValue) entry.getValue();
			buffer.put((byte) key.length());
			buffer.put(key.getBytes());
			value.encode(buffer);
		}

	}

	public int size() {
		if (type == DType.String) {
			if (data != null) {
				return ((String) data).getBytes().length;
			} else {
				return 0;
			}
		} else if (type == DType.Vector) {
			if (data != null) {
				return ((List<CAnyValue>) data).size();
			} else {
				return 0;
			}
		} else if (type == DType.Map) {
			if (data != null) {
				return ((Map<String, CAnyValue>) data).size();
			} else {
				return 0;
			}
		}
		return 0;
	}

	public boolean hasKey(String key) {
		if (type == DType.Map && data != null) {
			return ((Map<String, CAnyValue>) data).containsKey(key);
		}
		return false;
	}

	public Set<String> keys() {
		if (type == DType.Map && data != null) {
			return ((Map<String, CAnyValue>) data).keySet();
		} else {
			Set<String> setKeys = new HashSet<String>();
			return setKeys;
		}
	}

	public boolean isObject() {
		if (type == DType.Map && data != null) {
			return true;
		}
		return false;
	}

	public boolean isArray() {
		if (type == DType.Vector && data != null) {
			return true;
		}
		return false;
	}

	public boolean isString() {
		if (type == DType.String && data != null) {
			return true;
		}
		return false;
	}

	public boolean isBool() {
		if (type == DType.Bool && data != null) {
			return true;
		}
		return false;
	}

	public boolean isNull() {
		if ((type == DType.Null || type == -1) && data != null) {
			return true;
		}
		return false;
	}

	public void erase(String key) {
		if (type != DType.Map) {
			return;
		}
		((Map<String, CAnyValue>) data).remove(key);
	}

	public CAnyValue get(String key) {
		if (type != DType.Map) {
			return null;
		}
		return ((Map<String, CAnyValue>) data).get(key);
	}

	public CAnyValue get(int index) {
		if (type != DType.Vector) {
			return null;
		}
		return ((List<CAnyValue>) data).get(index);
	}

	public void put(String key, Object v) {
		if (type == -1) {
			initAsMap();
		}
		if (type != DType.Map) {
			return;
		}
		if (v instanceof CAnyValue) {
			((Map<String, CAnyValue>) data).put(key, (CAnyValue) v);
		} else {
			((Map<String, CAnyValue>) data).put(key, new CAnyValue(v));
		}
	}

	public int asInt() {
		if ((type == DType.Integer || type == DType.SInteger || type == DType.Bool) && data != null) {
			return (int) getUnsignedNum(data);
		}
		return 0;
	}

	public long asLong() {
		if ((type == DType.Integer || type == DType.SInteger || type == DType.Bool) && data != null) {
			return (long)getUnsignedNum(data);
		}
		return 0;
	}

	public short asShort() {
		if ((type == DType.Integer || type == DType.SInteger || type == DType.Bool) && data != null) {
			return (short)getUnsignedNum(data);
		}
		return 0;
	}
	
	public byte asByte() {
		if ((type == DType.Integer || type == DType.SInteger || type == DType.Bool) && data != null) {
			return (byte)getUnsignedNum(data);
		}
		return 0;
	}

	public String asString() {
		if (type == DType.String && data != null) {
			return (String) data;
		}
		return "";
	}
	
	public float asFloat() {
		if (type == DType.Float && data != null) {
			
			if(data instanceof Float)
			{
				return (Float)data;
			}
			else
			{
				return Float.valueOf(data.toString());
			}
		}
		return 0;
	}
	
	public double asDouble() {
		if (type == DType.Float && data != null) {
			if(data instanceof Double)
			{
				return (Double)data;
			}
			else
			{
				return Double.valueOf(data.toString());
			}
		}
		return 0.0;
	}

	public boolean asBool() {
		if (type == DType.Bool && data != null) {
			return (Boolean) data;
		}
		return false;
	}
	
	public Object asObject(Class<?> clazz)
	{
		try
		{
			Object obj = clazz.newInstance();
			Field []fields = clazz.getDeclaredFields();
			
			if (type != DType.Map) {
				return obj;
			}
	
			Map<String, CAnyValue> source = ((Map<String, CAnyValue>) data);

			for(int i=0;i<fields.length;i++)
			{
				System.out.println(fields[i].getType().getName());
				if(source.containsKey(fields[i].getName()))
				{
					if(fields[i].getType().getName() == "byte")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asByte());
					}
					if(fields[i].getType().getName() == "long")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asLong());
					}
					else if(fields[i].getType().getName() == "int")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asInt());
					}
					else if(fields[i].getType().getName() == "short")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asShort());
					}
					else if(fields[i].getType().getName() == "float")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asFloat());
					}
					else if(fields[i].getType().getName() == "double")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asDouble());
					}
					else if(fields[i].getType().getName() == "java.lang.String")
					{
						fields[i].setAccessible(true);
						fields[i].set(obj, source.get(fields[i].getName()).asString());
					}
				}
			}
			return obj;
		}
		catch(Exception e)
		{
			throw new Error(e.getMessage());
		}
	}

	public void clear() {
		type = -1;
		subtype = -1;
		data = null;
	}

	public void put(int index, Object v) {
		if (type == -1) {
			initAsArray();
		}
		if (type != DType.Vector) {
			return;
		}
		if (((List<CAnyValue>) data).size() <= index) {
			return;
		}
		if (v instanceof CAnyValue) {
			((List<CAnyValue>) data).set(index, (CAnyValue) v);
		} else {
			((List<CAnyValue>) data).set(index, new CAnyValue(v));
		}

	}

	public void put(Object v) {
		if (type == -1) {
			initAsArray();
		}
		if (type != DType.Vector) {
			return;
		}
		if (v instanceof CAnyValue) {
			((List<CAnyValue>) data).add((CAnyValue) v);
		} else {
			((List<CAnyValue>) data).add(new CAnyValue(v));
		}

	}
}


//自增长buffer 实现
class HeapBuffer {

	private byte []data = null;
	private ByteBuffer buffer = null;
	private int initSize = 0;
	private int maxSize = 0;
	private int currCapacity = 0;
	private static int DEFAULT_INIT_SIZE = 4096;
	private static int DEFAULT_MAX_SIZE = 1024*1024*100;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HeapBuffer buffer = HeapBuffer.allocate();
		buffer.putInt(1);
		buffer.putInt(1);
		buffer.putInt(1);
		buffer.putInt(1);
		buffer.putInt(1);
		buffer.putInt(1);
		byte []data = new byte[1000000];
		buffer.put(data);
		System.out.println(buffer.currCapacity());
	}
	
	public static HeapBuffer allocate()
	{
		return new HeapBuffer(DEFAULT_INIT_SIZE, DEFAULT_MAX_SIZE);
	}
	
	public static HeapBuffer wrap(byte[] src)
	{
		return new HeapBuffer(src);
	}
	
	public static HeapBuffer wrap(byte[] src,int offset,int length)
	{
		return new HeapBuffer(src,offset,length);
	}
	
	protected HeapBuffer(byte[] src)
	{
		data = src;
		currCapacity = src.length;
		this.initSize = currCapacity;
		this.maxSize = currCapacity;
		buffer = ByteBuffer.wrap(data);
	}
	
	protected HeapBuffer(byte[] src,int offset,int length)
	{
		data = src;
		currCapacity = src.length;
		this.initSize = currCapacity;
		this.maxSize = currCapacity;
		buffer = ByteBuffer.wrap(data,offset,length);
	}
	
	protected HeapBuffer(int initSize,int maxSize)
	{
		data = new byte[initSize];
		currCapacity = initSize;
		this.initSize = initSize;
		this.maxSize = maxSize;
		buffer = ByteBuffer.wrap(data);
	}
	
	public byte get()
	{
		return buffer.get();
	}
	
	public byte get(int index)
	{
		return buffer.get(index);
	}
	
	public HeapBuffer get(byte[] dst)
	{
		buffer.get(dst);
		return this;
	}
	
	public HeapBuffer get(byte[] dst,int offset,int length)
	{
		buffer.get(dst,offset,length);
		return this;
	}
	
	public char getChar()
	{
		return buffer.getChar();
	}

	public char getChar(int index)
	{
		return buffer.getChar(index);
	}

	public double getDouble()
	{
		return buffer.getDouble();
	}
	public double getDouble(int index)
	{
		return buffer.getDouble(index);
	}
	public float getFloat()
	{
		return buffer.getFloat();
	}
	public float getFloat(int index)
	{
		return buffer.getFloat(index);
	}
	
	public int getInt()
	{
		return buffer.getInt();
	}
	public int getInt(int index)
	{
		return buffer.getInt(index);
	}
	public long getLong()
	{
		return buffer.getLong();
	}
	public long getLong(int index)
	{
		return buffer.getLong(index);
	}
	
	public short getShort()
	{
		return buffer.getShort();
	}
	public short getShort(int index)
	{
		return buffer.getShort(index);
	}
	
	private void expand(int need)
	{
		currCapacity = (currCapacity+need)*2;
		if(currCapacity > maxSize) 
		{
			currCapacity = maxSize;
		}
		byte [] preData = data;
		data = new byte[currCapacity];
		
		System.arraycopy(preData, 0, data, 0, preData.length);
		
		int position = buffer.position();
		buffer = ByteBuffer.wrap(data);
		buffer.position(position);
	}
	
	public boolean check(int offset)
	{
		if(buffer.position()+offset > currCapacity)
		{
			return false;
		}
		return true;
	}
	
	public HeapBuffer put(byte b)
	{
		if(!check(1)) 
		{
			expand(1);
		}	
		buffer.put(b);
		return this;
	}
	
	public HeapBuffer put(byte[] src)
	{
		if(!check(src.length)) 
		{
			expand(src.length);
		}
		buffer.put(src);
		return this;
	}
	
	public HeapBuffer put(int index,byte b)
	{
		buffer.put(index, b);
		return this;
	}
	
	public HeapBuffer put(byte[] src,int offset,int length)
	{
		if(!check(length)) 
		{
			expand(length);
		}
		buffer.put(src, offset, length);
		return this;
	}
	
	public HeapBuffer putChar(char value)
	{
		if(!check(2)) 
		{
			expand(2);
		}
		buffer.putChar(value);
		return this;
	}
	
	public HeapBuffer putChar(int index ,char value)
	{
		buffer.putChar(index,value);
		return this;
	}
	
	public HeapBuffer putShort(short value)
	{
		if(!check(2)) 
		{
			expand(2);
		}
		buffer.putShort(value);
		return this;
	}
	
	public HeapBuffer putShort(int index ,short value)
	{
		buffer.putShort(index,value);
		return this;
	}
	
	public HeapBuffer putInt(int value)
	{
		if(!check(4)) 
		{
			expand(4);
		}
		buffer.putInt(value);
		return this;
	}
	
	public HeapBuffer putShort(int index ,int value)
	{
		buffer.putInt(index,value);
		return this;
	}
	
	public HeapBuffer putLong(long value)
	{
		if(!check(8)) 
		{
			expand(8);
		}
		buffer.putLong(value);
		return this;
	}
	
	public HeapBuffer putLong(int index ,long value)
	{
		buffer.putLong(index,value);
		return this;
	}
	
	public HeapBuffer putFloat(float value)
	{
		if(!check(4)) 
		{
			expand(4);
		}
		buffer.putFloat(value);
		return this;
	}
	
	public HeapBuffer putFloat(int index ,float value)
	{
		buffer.putFloat(index,value);
		return this;
	}
	
	public HeapBuffer putDouble(double value)
	{
		if(!check(8)) 
		{
			expand(8);
		}
		buffer.putDouble(value);
		return this;
	}
	
	public HeapBuffer putDouble(int index ,double value)
	{
		buffer.putDouble(index,value);
		return this;
	}
	
	public int capacity()
	{
		return maxSize;
	}
	
	public int position()
	{
		return buffer.position();
	}
	
	public HeapBuffer position(int pos)
	{
		buffer.position(pos);
		return this;
	}
	
	public HeapBuffer mark()
	{
		buffer.mark();
		return this;
	}
	
	public HeapBuffer reset()
	{
		buffer.reset();
		return this;
	}
	
	public int currCapacity()
	{
		return currCapacity;
	}
	
	public byte[] array()
	{
		return buffer.array();
	}
}



