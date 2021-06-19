package gu.dtalk;

import java.util.regex.Pattern;

import net.gdface.utils.FaceUtilits;

public class HexOption extends BaseOption<byte[]> {
	private static final Pattern PATTERN_HEX = Pattern.compile("^[0-9A-Fa-f]+$");

	@Override
	public final OptionType getType() {
		return OptionType.HEX;
	}

	@Override
	public String toString(byte[] input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return FaceUtilits.toHex(input);
	}

	@Override
	public byte[] fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		if(((input.length() & 1) == 0) && PATTERN_HEX.matcher(input).matches()){
			return FaceUtilits.hex2Bytes(input);
		}
        throw new TransformException("INVALID HEX");
	}

}
