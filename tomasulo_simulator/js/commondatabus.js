Array.prototype.indexOf = function(val) {              
    for (var i = 0; i < this.length; i++) {  
        if (this[i] == val) return i;  
    }  
    return -1;  
};  
Array.prototype.remove = function(val) {  
    var index = this.indexOf(val);  
    if (index > -1) {  
        this.splice(index, 1);  
    }  
}; 

class CommonDataBus {
	constructor() {
		this._busy = {};
		this._result = {};
	}
	getBusy(type, name) {
		var res;
		if(this._busy[type+'.'+name])
			res = this._busy[type+'.'+name][0]
		if (typeof res === 'undefined') {
			return null;
		}
		else{
			// alert(res.name);
			return res;
		}
	}
	setBusy(type, name, rs) {
		if (!this._busy[type+'.'+name] || typeof this._busy[type+'.'+name] === 'undefined') {
			this._busy[type+'.'+name] = new Array();
		}
		this._busy[type+'.'+name].push(rs);
	}
	setUnbusy(type, name, rs) {
		this._busy[type+'.'+name].remove(rs);
	}
	getResult(station) {
		if(station.instruction) {
			var res = this._result[''+station.instruction.pc];
		if (typeof res === 'undefined') 
			return null;
		return res;
		}
		return null;
	}
	setResult(station, value) {
		this._result[''+station.instruction.pc] = value;
	}
	clearResult() {
		this._result = {};
	}
}