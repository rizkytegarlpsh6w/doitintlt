package gu.dtalk;

import java.util.regex.Pattern;

public abstract class BoolOption extends BaseOption<Boolean> {
	private static final Pattern PATTERN_TRUE = Pattern.compile("^(yes|1|on|true)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_FALSE = Pattern.compile("^(no|0|off|false)$", Pattern.CASE_INSENSITIVE);
	public BoolOption() {
	}

	@Override
	public final OptionType getType() {
		return OptionType.BOOL;
	}

	@Override
	public String toString(Boolean input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return input.toString();
	}

	@Override
	public Boolean fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
        if( PATTERN_TRUE.matcher(input).matches()){
        	return true;
        }else if(PATTERN_FALSE.matcher(input).matches()){
        	this.optionValue = false;
        	return false;
        }
        throw new TransformException("INVALID INPUT");
	}

}
