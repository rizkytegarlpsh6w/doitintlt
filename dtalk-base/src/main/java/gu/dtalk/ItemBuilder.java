package gu.dtalk;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Throwables;

public class ItemBuilder<T extends BaseItem> {

	private final T item;
	protected ItemBuilder(T item) {
		super();
		this.item = item;
	}

	public ItemBuilder<T> name(String name) {
		item.setName(name);
		return this;
	}

	public ItemBuilder<T> disable(boolean disable) {
		item.setDisable(disable);
		return this;
	}

	public ItemBuilder<T> description(String description) {
		item.setDescription(description);
		return this;
	}

	public ItemBuilder<T> uiName(String uiName) {
		item.setUiName(uiName);
		return this;
	}

	public ItemBuilder<T> childs(List<BaseItem> childs) {
		checkState(item.isContainer(),"%s isn't container",item.getClass().getSimpleName());
		item.setChilds(childs);
		return this;
	}

	public ItemBuilder<T> addChilds(BaseItem... childs) {
		checkState(item.isContainer(),"%s isn't container",item.getClass().getSimpleName());
		item.addChilds(childs);
		return this;
	}

	public ItemBuilder<T> addChilds(Collection<BaseItem> childs) {
		checkState(item.isContainer(),"%s isn't container",item.getClass().getSimpleName());
		item.addChilds(childs);
		return this;
	}

	public T instance(){
		return item;
	}
	public static <T extends BaseItem>ItemBuilder<?> builder(Class<T> type) {
		checkNotNull(type,"type is null");
		// 不允许为抽象类
		checkArgument(!Modifier.isAbstract(type.getModifiers()),"%s is a abstract class",type.getName());
		try {
			return new ItemBuilder<T>(type.newInstance());
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}
