package gu.dtalk;

public class IntOption extends BaseNumOption<Integer> {

	@Override
	public final OptionType getType() {
		return OptionType.INTEGER;
	}

	@Override
	public Integer fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		try{
			return Integer.valueOf(input);
		}catch(NumberFormatException e){
			throw new TransformException(e);
		}
	}
}
