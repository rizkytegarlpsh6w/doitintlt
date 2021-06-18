package gu.dtalk;

public class FloatOption extends BaseNumOption<Float> {

	@Override
	public final OptionType getType() {
		return OptionType.FLOAT;
	}

	@Override
	public Float fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		try{
			return Float.valueOf(input);
		}catch(NumberFormatException e){
			throw new TransformException(e);
		}
	}

}
