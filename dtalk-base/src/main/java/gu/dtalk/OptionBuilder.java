package gu.dtalk;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import gu.dtalk.event.ValueListener;

/**
 * 选项实例构造器
 * @author guyadong
 *
 * @param <T> 选项的数据类型
 * @param <O>选项的实现类型
 */
public class OptionBuilder<T,O extends BaseOption<T>> {

	private final O option;
	public OptionBuilder(O item) {
		super();
		this.option = item;
	}
	public OptionBuilder<T,O> name(String name) {
		option.setName(name);
		return this;
	}

	public OptionBuilder<T,O> disable(boolean disable) {
		option.setDisable(disable);
		return this;
	}
	public OptionBuilder<T,O> disable() {
		return disable(true);
	}
	public OptionBuilder<T, O> hide(boolean hide) {
		option.setHide(hide);
		return this;
	}
	public OptionBuilder<T, O> hide() {
		return hide(true);
	}
	public OptionBuilder<T,O> readonly(boolean readOnly) {
		option.setReadOnly(readOnly);
		return this;
	}
	public OptionBuilder<T,O> readonly() {
		return readonly(true);
	}
	public OptionBuilder<T,O> needReset(boolean needReset) {
		option.setNeedReset(needReset);
		return this;
	}
	public OptionBuilder<T,O> needReset() {
		return needReset(true);
	}
	public OptionBuilder<T,O> required(boolean required) {
		option.setRequired(required);
		return this;
	}
	public OptionBuilder<T,O> regex(String regex) {
		option.setRegex(regex);
		return this;
	}
	public OptionBuilder<T,O> required() {
		return required(true);
	}
	public OptionBuilder<T,O> description(String description) {
		option.setDescription(description);
		return this;
	}

	public OptionBuilder<T,O> uiName(String uiName) {
		option.setUiName(uiName);
		return this;
	}

	public OptionBuilder<T,O> value(T value) {
		option.setValue(value);
		return this;
	}
	public OptionBuilder<T,O> defaultValue(T value) {
		option.setDefaultValue(value);
		return this;
	}
	public OptionBuilder<T,O> asValue(String value) {
		option.asValue(value);
		return this;
	}
	public OptionBuilder<T,O> asDefaultValue(String value) {
		option.asDefaultValue(value);
		return this;
	}
	public OptionBuilder<T,O> validator(Predicate<T> validator) {
		option.setValidator(validator);
		return this;
	}
	public final OptionBuilder<T, O> available(List<T> available) {
		option.setAvaiable(available);
		return this;
	}
	@SuppressWarnings("unchecked")
	public final OptionBuilder<T, O> available(T... values) {
		option.clearAvailable();
		option.addAvailable(values);
		return this;
	}
	/**
	 * 添加事件侦听器
	 * @param listeners  侦听器列表
	 * @return 当前对象
	 */
	@SafeVarargs
	public final OptionBuilder<T, O> addListener(ValueListener<T> ...listeners) {
		option.addListener(listeners);
		return this;
	}
	/**
	 * @return 返回option对象
	 * @throws IllegalArgumentException 返回前检查value,defaultValue的有效性，无效则抛出异常
	 */
	public O instance(){
		option.compile();
		return option;
	}
	public static <T,O extends BaseOption<T>>OptionBuilder<T,O> builder(O instance) {
		return new OptionBuilder<T,O>(checkNotNull(instance,"instance is null"));
	}
	public static <T,O extends BaseOption<T>>OptionBuilder<T,O> builder(Class<O> type) {
		// 不允许为抽象类
		checkArgument(!Modifier.isAbstract(type.getModifiers()),"%s is a abstract class",type.getName());
		try {
			return new OptionBuilder<T,O>(type.newInstance());
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T, O extends BaseOption<T>>OptionBuilder<T,O> builder(OptionType optionType) {
		Class<T> type = (Class<T>) checkNotNull(optionType,"optionType is null").optClass;
		try {
			return new OptionBuilder<T,O>((O)((O) type.newInstance()).setType(optionType));
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}
