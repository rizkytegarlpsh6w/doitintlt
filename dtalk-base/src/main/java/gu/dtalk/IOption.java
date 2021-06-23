package gu.dtalk;

public interface IOption extends IItem {
	OptionType getType();
	boolean isReadOnly();
	boolean isValid(String value);
	boolean setValue(String value);
	String getValue();
	String getDefaultValue();
	<V>V getObjectValue();
}
