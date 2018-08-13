package com.solinia.solinia.Models;

import com.solinia.solinia.Interfaces.ISoliniaLootTableEntry;

public class SoliniaLootTableEntry implements ISoliniaLootTableEntry {
	private int id;
	private int lootdropid;
	private int loottableid;
	private boolean operatorCreated = true;
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getLootdropid() {
		return lootdropid;
	}

	@Override
	public void setLootdropid(int lootdropid) {
		this.lootdropid = lootdropid;
	}
	
	@Override
	public int getLoottableid(int loottableid) {
		return this.loottableid;
	}

	@Override
	public void setLoottableid(int loottableid) {
		this.loottableid = loottableid;
	}
	
	@Override
	public void setOperatorCreated(boolean operatorCreated)
	{
		this.operatorCreated = operatorCreated;
	}
	
	@Override
	public boolean isOperatorCreated()
	{
		return this.operatorCreated;
	}
}
