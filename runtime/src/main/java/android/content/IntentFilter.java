package android.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntentFilter {
    public static final int SYSTEM_HIGH_PRIORITY = 1000, SYSTEM_LOW_PRIORITY = -1000;
    private final List<String> actions = new ArrayList<>(), categories = new ArrayList<>(), schemes = new ArrayList<>();
    private int priority;

    public IntentFilter() {}
    public IntentFilter(String action) { actions.add(action); }
    public IntentFilter(String action, String dataType) { actions.add(action); }
    public IntentFilter(IntentFilter o) { actions.addAll(o.actions); categories.addAll(o.categories); schemes.addAll(o.schemes); priority = o.priority; }

    public final void addAction(String action) { if (!actions.contains(action)) actions.add(action); }
    public final int countActions() { return actions.size(); }
    public final String getAction(int index) { return actions.get(index); }
    public final boolean hasAction(String action) { return action != null && actions.contains(action); }
    public final boolean matchAction(String action) { return hasAction(action); }
    public final Iterator<String> actionsIterator() { return actions.iterator(); }
    public final void addCategory(String category) { categories.add(category); }
    public final boolean hasCategory(String category) { return categories.contains(category); }
    public final void addDataScheme(String scheme) { schemes.add(scheme); }
    public final boolean hasDataScheme(String scheme) { return schemes.contains(scheme); }
    public final void setPriority(int priority) { this.priority = priority; }
    public final int getPriority() { return priority; }
}
