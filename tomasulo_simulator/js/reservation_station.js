var STATUS = {
	'IDLE': 0,
	'ISSUE': 1,
	'EXEC': 2,
	'WRITE': 3
};

class ReservationStation {
	constructor (name) {
		this.name = name;
		this.state = STATUS.IDLE;
		this.para = null;
		this.tags = null;
		this.instruction = null;
	}
}