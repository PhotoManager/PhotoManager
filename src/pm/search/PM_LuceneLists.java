/*
 * photo-manager is a program to manage and organize your photos; Copyright (C) 2010 Dietrich Hentschel
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package pm.search;

import pm.picture.*;
import pm.utilities.*;
import pm.utilities.PM_Interface.IndexType;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;

import java.util.*;

/**
 * Manage the indices
 * 
 * !!!!!!!!!!! Singleton !!!!!!!!!!
 * 
 * 
 */
public class PM_LuceneLists implements PM_Interface {

	private static PM_LuceneLists luceneIndexInstance = null;
	private Vector<PM_Listener> alleListener = new Vector<PM_Listener>();

	private final List<String> listIndex_1 = new ArrayList<String>();
	private final List<String> listIndex_2 = new ArrayList<String>();

	private PM_Listener metadatenChangeListener;

	static public PM_LuceneLists getInstance() {
		if (luceneIndexInstance == null)
			luceneIndexInstance = new PM_LuceneLists();
		return luceneIndexInstance;
	}

	private PM_LuceneLists() {
		metadatenChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				if (e.getObject() instanceof PM_Picture) {
					PM_Picture picture = (PM_Picture) e.getObject();

					int type = e.getType();
					if (type == PM_PictureMetadaten.INDEX_1) {
						updateIndex(picture.meta.getIndex1(), listIndex_1,
								picture, IndexType.INDEX_1);
					}
					if (type == PM_PictureMetadaten.INDEX_2) {
						updateIndex(picture.meta.getIndex2(), listIndex_2,
								picture, IndexType.INDEX_2);
					}
				}
			}
		};

		PM_PictureMetadaten.addChangeListener(metadatenChangeListener);

	}

	public void initAddIndex1(String index) {
		String[] alle = index.split(" ");
		for (String str : alle) {
			addToList(listIndex_1, str);
		}
	}

	public void initAddIndex2(String index) {
		String[] alle = index.split(" ");
		for (String str : alle) {
			addToList(listIndex_2, str);
		}
	}

	public void initComplete() {
		Collections.sort(listIndex_1);
		Collections.sort(listIndex_2);
	}

	public List<String> getIndex_1() {
		return listIndex_1;
	}

	public List<String> getIndex_2() {
		return listIndex_2;
	}

	private boolean addToList(List<String> list, String str) {
		if (str.length() == 0) {
			return false;
		}
		if (!list.contains(str)) {
			list.add(str);
			return true;
		}
		return false;		
	}
	
	public void addListener(PM_Listener listener) {
		if (!alleListener.contains(listener))
			alleListener.add(listener);
	}

	private void updateIndex(String index, List<String> list,
			PM_Picture picture, IndexType indexType) {
		String[] alle = index.split(" ");
		boolean changed = false;
		for (String str : alle) {
			if (!list.contains(str)) {
				changed = addToList(list, str);
			}
		}
		if (changed) {
			Collections.sort(list);
			for (PM_Listener l : alleListener) {
				l.actionPerformed(new PM_Action(indexType));
			}
		}
	}
	
	public String changeIndexName(IndexType type, String strOld, String strNew, Vector<String> vector) {
		String ind;
		List<String> data;
		if (type == IndexType.INDEX_2) {			 
			data = PM_LuceneLists.getInstance().getIndex_2();
			ind = PM_LuceneDocument.LUCENE_INDEX2;
		} else {
			data = PM_LuceneLists.getInstance().getIndex_1();
			ind = PM_LuceneDocument.LUCENE_INDEX1;
		}
		if (strNew.length() == 0) {
			return "Neuer Name unzulässig";
		}
		if (!data.contains(strOld)) {
			return "Alter Name nigefu";
		}
		if (data.contains(strNew)) {
			return "Neuer Name bereits vorhanden";
		}
		
		// Change now
		String searchString = ind + ":" + strOld;
		PM_Search search = new PM_Search(new PM_SearchExpr(SearchType.NORMAL,
				searchString));
		search.search();
		List<PM_Picture> list = search.getPictureList(SearchSortType.NOTHING);
		for (PM_Picture p: list) {
			if (type == IndexType.INDEX_2) {			 
				ind = p.meta.getIndex2();
				p.meta.setIndex2(ind.replaceAll(strOld, strNew));
			} else {
				ind = p.meta.getIndex1(); 
				p.meta.setIndex1(ind.replaceAll(strOld, strNew)); 
			}
		}
		data.remove(strOld);
		vector.remove(strOld);
		vector.add(strNew);
		Collections.sort(vector);
		 
		Collections.sort(data);
		for (PM_Listener l : alleListener) {
			l.actionPerformed(new PM_Action(type));
		}
		
		return "geändert: " + list.size() + " pictures";
		
	}

}
