package gu.dtalk;

import java.util.Date;

import com.alibaba.fastjson.TypeReference;

/**
 * 日期选项
 * @author guyadong
 *
 */
public class DateOption extends BaseOption<Date> {
    public DateOption() {
		super(new TypeReference<Date>() {}.getType());
	}
    private int fieldRequire=3;
    /**
     *  @return 
     * 返回设备端要求的日期类型<br>
     * <ul>
     * <li> 0: default:date and time(日期+时间)</li>
     * <li> 1: date only(只要日期)</li>
     *  <li>2: time only(只要时间)</li>
     *  <li>3:date and time(日期+时间)</li>
     *  </ul>
     */
    public int getFieldRequire(){
		return fieldRequire;
    } 
    /**
     * 设置设备端要求的日期类型<br>
     * @param fieldRequire
     * <ul>
     * <li> 1: date only(只要日期)</li>
     *  <li>2: time only(只要时间)</li>
     *  <li>other:date and time(日期+时间)</li>
     *  </ul>
     * @return 当前对象
     */
    public DateOption setFieldRequire(int fieldRequire){
    	switch (fieldRequire) {
		case 1:
		case 2:	
			this.fieldRequire=fieldRequire;
			break;
		default:
			this.fieldRequire=3;
			break;
		}
		return this;    	
    }
	@Override
	public final OptionType getType() {
		return OptionType.DATE;
	}

}
