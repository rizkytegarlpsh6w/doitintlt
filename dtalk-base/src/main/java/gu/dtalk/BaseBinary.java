package gu.dtalk;

import net.gdface.utils.FaceUtilits;

public abstract class BaseBinary extends BaseOption<byte[]> {

	public BaseBinary() {
		super(byte[].class);
	}

	@Override
	public String contentOfValue() {
		byte[] value = (byte[])getValue();
		if(value == null){
			return super.contentOfValue();
		}
		if(value.length <=32){
			return FaceUtilits.toHex(value);
		}
		return "BINARY";
	}
	
}
