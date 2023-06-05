package gu.dtalk;

/**
 * 浮点数类型选项
 * @author guyadong
 *
 */
public class FloatOption extends BaseNumOption<Double> {
	/** 显示精度:小数点位数,默认未定义*/
	private Integer precision ;
	public FloatOption() {
		super(0.0);
	}

	@Override
	public final OptionType getType() {
		return OptionType.FLOAT;
	}

	public Integer getPrecision() {
		return precision;
	}

	public FloatOption setPrecision(Integer precision) {
		if(precision == null || precision >= 0){
			this.precision = precision;
		}
		return this;
	}

}
