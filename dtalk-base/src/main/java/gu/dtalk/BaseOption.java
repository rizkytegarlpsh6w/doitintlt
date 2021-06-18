package gu.dtalk;

public abstract class BaseOption<T> extends BaseItem implements IOption,StringTransformer<T> {
	protected T optionValue;
	protected T defaultValue;

	public BaseOption() {
	}
	@Override
	public boolean isReadOnly() {
		return false;
	}
	@Override
	public final boolean isContainer() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}
	@Override
	public final boolean isValid(String value) {
		try{
			fromString(value);
			return true;
		}catch (TransformException e) {
			return false;
		}
	}

	@Override
	public final String getValue() {
		try{
			return	toString(optionValue);
		}catch (TransformException e) {
			return e.getMessage();
		}catch (Exception e) {
			return "ERROR VALUE";
		}
	}
	@Override
	public boolean setValue(String value) {
		try{
			optionValue =	fromString(value);
			return true;
		}catch (Exception e) {
			return false;
		}
	}
	@Override
	public final String getDefaultValue() {
		try{
			return toString(defaultValue);
		}catch (TransformException e) {
			return e.getMessage();
		}catch (Exception e) {
			return "ERROR DEFAUTL VALUE";
		}
	}
	
}
