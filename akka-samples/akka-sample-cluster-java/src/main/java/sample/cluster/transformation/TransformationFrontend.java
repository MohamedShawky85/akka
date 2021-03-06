package sample.cluster.transformation;

import static sample.cluster.transformation.TransformationMessages.BACKEND_REGISTRATION;

import java.util.ArrayList;
import java.util.List;

import sample.cluster.transformation.TransformationMessages.JobFailed;
import sample.cluster.transformation.TransformationMessages.TransformationJob;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.AbstractActor;

//#frontend
public class TransformationFrontend extends AbstractActor {

  List<ActorRef> backends = new ArrayList<ActorRef>();
  int jobCounter = 0;

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(TransformationJob.class, job -> backends.isEmpty(), job -> {
        sender().tell(
          new JobFailed("Service unavailable, try again later", job),
          sender());
      })
      .match(TransformationJob.class, job -> {
        jobCounter++;
        backends.get(jobCounter % backends.size())
          .forward(job, getContext());
      })
      .matchEquals(BACKEND_REGISTRATION, m -> {
        getContext().watch(sender());
        backends.add(sender());
      })
      .match(Terminated.class, terminated -> {
        backends.remove(terminated.getActor());
      })
      .build();
  }
}
//#frontend
