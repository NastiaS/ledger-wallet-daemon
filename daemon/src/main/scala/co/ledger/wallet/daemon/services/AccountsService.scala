package co.ledger.wallet.daemon.services

import java.util.UUID

import co.ledger.core.implicits.{AccountNotFoundException, WalletNotFoundException}
import co.ledger.wallet.daemon.async.MDCPropagatingExecutionContext
import co.ledger.wallet.daemon.database.DaemonCache
import co.ledger.wallet.daemon.database.DefaultDaemonCache.User
import co.ledger.wallet.daemon.models.Account.Account
import co.ledger.wallet.daemon.models.Operations.{OperationView, PackedOperationsView}
import co.ledger.wallet.daemon.models._
import co.ledger.wallet.daemon.schedulers.observers.SynchronizationResult
import co.ledger.wallet.daemon.utils
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountsService @Inject()(defaultDaemonCache: DaemonCache) extends DaemonService {
  implicit val ec: ExecutionContext = MDCPropagatingExecutionContext.Implicits.global

  def accounts(user: User, poolName: String, walletName: String): Future[Seq[AccountView]] = {
    defaultDaemonCache.getAccounts(user.pubKey, poolName, walletName).flatMap { accounts =>
      Future.sequence(accounts.map { account => account.accountView })
    }
  }

  def account(accountIndex: Int, user: User, poolName: String, walletName: String): Future[Option[AccountView]] = {
    defaultDaemonCache.getAccount(accountIndex, user.pubKey, poolName, walletName).flatMap {
      case Some(account) => account.accountView.map(Option(_))
      case None => Future(None)
    }
  }

  def synchronizeAccount(accountIndex: Int, user: User, poolName: String, walletName: String): Future[Seq[SynchronizationResult]] ={
     defaultDaemonCache.syncOperations(user.pubKey, poolName, walletName, accountIndex)
  }

  def getAccount(accountIndex: Int, user: User, poolName: String, walletName: String): Future[Option[Account]] = {
    defaultDaemonCache.getAccount(accountIndex, user.pubKey, poolName, walletName)
  }

  def accountFreshAddresses(accountIndex: Int, user: User, poolName: String, walletName: String): Future[Seq[String]] = {
    defaultDaemonCache.getFreshAddresses(accountIndex, user, poolName, walletName)
  }

  def accountDerivationPath(accountIndex: Int, user: User, poolName: String, walletName: String): Future[String] = {
    defaultDaemonCache.getDerivationPath(accountIndex, user.pubKey, poolName, walletName)
  }

  def nextAccountCreationInfo(user: User, poolName: String, walletName: String, accountIndex: Option[Int]): Future[AccountDerivationView] = {
    defaultDaemonCache.getNextAccountCreationInfo(user.pubKey, poolName, walletName, accountIndex).map(_.view)
  }

  def nextExtendedAccountCreationInfo(user: User, poolName: String, walletName: String, accountIndex: Option[Int]): Future[AccountExtendedDerivationView] = {
    defaultDaemonCache.getNextExtendedAccountCreationInfo(user.pubKey, poolName, walletName, accountIndex).map(_.view)
  }

  def accountOperations(
                         user: User,
                         accountIndex: Int,
                         poolName: String,
                         walletName: String,
                         queryParams: OperationQueryParams): Future[PackedOperationsView] = {
    if(queryParams.next.isEmpty && queryParams.previous.isEmpty) {
      // new request
      info(LogMsgMaker.newInstance("Retrieve latest operations").toString())
      defaultDaemonCache.getAccountOperations(user, accountIndex, poolName, walletName, queryParams.batch, queryParams.fullOp)
    } else if (queryParams.next.isDefined) {
      // next has more priority, using database batch instead queryParams.batch
      info(LogMsgMaker.newInstance("Retrieve next batch operation").toString())
      defaultDaemonCache.getNextBatchAccountOperations(user, accountIndex, poolName, walletName, queryParams.next.get, queryParams.fullOp)
    } else {
      info(LogMsgMaker.newInstance("Retrieve previous operations").toString())
      defaultDaemonCache.getPreviousBatchAccountOperations(user, accountIndex, poolName, walletName, queryParams.previous.get, queryParams.fullOp)
    }
  }

  def accountOperation(user: User, uid: String, accountIndex: Int, poolName: String, walletName: String, fullOp: Int): Future[Option[OperationView]] = {
    (for {
      walletOpt <- defaultDaemonCache.getWallet(walletName, poolName, user.pubKey)
      wallet = walletOpt.getOrElse(throw new WalletNotFoundException(s"Wallet '$walletName' not found"))
      accountOpt <- wallet.account(accountIndex)
      account = accountOpt.getOrElse(throw new AccountNotFoundException(s"Account '$accountIndex' not found"))
      operation <- account.operation(uid, fullOp)
    } yield {
      utils.optionSequence(operation.map { op => Operations.getView(op, wallet, account) })
    }).flatten
  }

  def createAccount(accountCreationBody: AccountDerivationView, user: User, poolName: String, walletName: String): Future[AccountView] = {
    defaultDaemonCache.createAccount(accountCreationBody, user, poolName, walletName).flatMap(_.accountView)
  }

  def createAccountWithExtendedInfo(derivations: AccountExtendedDerivationView, user: User, poolName: String, walletName: String): Future[AccountView] = {
    defaultDaemonCache.createAccount(derivations, user, poolName, walletName).flatMap(_.accountView)
  }

}

case class OperationQueryParams(previous: Option[UUID], next: Option[UUID], batch: Int, fullOp: Int)