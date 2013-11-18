import hammurabi._

trait SimpleScalaModel {
	def test() = {
		println("SimpleScalaModel") 
	}
}

trait ScalaModelWithDependencies {
	val rule: Rule

	def test() = {
		println("ScalaModelWithDependencies") 
	}
}
