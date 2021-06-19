package gu.dtalk;

import net.gdface.utils.Base64Utils;

public class Base64Option extends BaseOption<byte[]> {

	@Override
	public OptionType getType() {
		return OptionType.BASE64;
	}

	@Override
	public String toString(byte[] input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return Base64Utils.encode(input);
	}

	@Override
	public byte[] fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return Base64Utils.decode(input);
	}



}
