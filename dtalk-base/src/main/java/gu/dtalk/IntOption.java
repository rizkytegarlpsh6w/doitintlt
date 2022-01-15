package gu.dtalk;

/**
 * 整数类型选项
 * @author guyadong
 *
 */
public class IntOption extends BaseNumOption<Integer> {

	public IntOption() {
		super(0);
	}

	@Override
	public final OptionType getType() {
		return OptionType.INTEGER;
	}
	
}
