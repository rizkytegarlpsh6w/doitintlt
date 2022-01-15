package gu.dtalk;

/**
 * 浮点数类型选项
 * @author guyadong
 *
 */
public class FloatOption extends BaseNumOption<Double> {

	public FloatOption() {
		super(0.0);
	}

	@Override
	public final OptionType getType() {
		return OptionType.FLOAT;
	}

}
