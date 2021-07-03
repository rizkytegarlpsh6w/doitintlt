package gu.dtalk;

public interface IOption extends IItem {
	OptionType getType();
	boolean isReadOnly();
	boolean setValue(Object value);
	Object getValue();
	Object getDefaultValue();
	boolean isReqiured();
}
