package edu.gmu.horde

import edu.gmu.horde.train.StandaloneAgentTrainer
import StandaloneAgentTrainer.HordePosition
import org.scalatest._

class StandaloneAgentTrainerSpec extends WordSpecLike {

  "Horde Posistion" must {

    "be equal if values are the same" in {
      val p = HordePosition(1, 1)
      val q = HordePosition(1, 1)
      assert(p == q)
      assert(p == HordePosition(new Integer(1), new Integer(1)))
    }
  }
}