class Buffer extends ReservationStation{
	constructor(name, memory) {
		// ReservationStation.apply(this, arguments);
		//继承保留站类
		super(name);
		this.memory = memory;
	}
}