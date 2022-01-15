package gu.dtalk;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Modifier;

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
	public OptionBuilder<T,O> readonly(boolean readOnly) {
		option.setReadOnly(readOnly);
		return this;
	}
	public OptionBuilder<T,O> required(boolean required) {
		option.setRequired(required);
		return this;
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
	/**
	 * 添加事件侦听器
	 * @param listeners
	 * @return
	 */
	@SafeVarargs
	public final OptionBuilder<T, O> addListener(ValueListener<T> ...listeners) {
		option.addListener(listeners);
		return this;
	}
	/**
	 * 返回<T>对象
	 * @return
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
