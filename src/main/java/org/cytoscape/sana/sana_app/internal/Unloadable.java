package org.cytoscape.sana.sana_app.internal;

import java.util.LinkedList;

interface IUnloadable {
	void unload();
}

public abstract class Unloadable implements IUnloadable {
	public Unloadable() {
		unloadLater(this);
	}

	public void unloadNow() {
		unloadNow(this);
	}

	static synchronized void unloadLater(IUnloadable u) {
		if (_unloadables == null)
			_unloadables = new LinkedList<IUnloadable>();
		// Add in reverse order so that classes created early get unloaded the
		// last
		_unloadables.addFirst(u);
	}

	static synchronized void unloadNow(IUnloadable u) {
		if (_unloadables != null)
			_unloadables.remove(u);
	}

	static LinkedList<IUnloadable> _unloadables;

	static synchronized void unloadAll() {
		LinkedList<IUnloadable> ulist = _unloadables;
		if (ulist != null) {
			_unloadables = null;
			for (IUnloadable u : ulist)
				u.unload();
		}
	}
}
