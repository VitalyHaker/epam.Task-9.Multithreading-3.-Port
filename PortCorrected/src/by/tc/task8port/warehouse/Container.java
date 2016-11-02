package by.tc.task8port.warehouse;

public class Container {
	private int id;
	
	public Container(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	@Override // переопределяем метод hashCode()
	public int hashCode() {
		return id*16;
	}
	
	@Override // переопределяем метод equals()
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Container container = (Container) o;

		return id == container.id;

	}

}
