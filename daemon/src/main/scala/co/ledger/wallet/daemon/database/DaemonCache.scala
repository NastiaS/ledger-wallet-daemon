package co.ledger.wallet.daemon.database

import java.util.UUID

import co.ledger.wallet.daemon.database.DefaultDaemonCache.User
import co.ledger.wallet.daemon.models.Account.{Account, Derivation, ExtendedDerivation}
import co.ledger.wallet.daemon.models.Operations.{OperationView, PackedOperationsView}
import co.ledger.wallet.daemon.models._
import co.ledger.wallet.daemon.schedulers.observers.SynchronizationResult

import scala.concurrent.Future

trait DaemonCache {

  // ************** account *************
  /**
    * Method to create an account instance. The account may already exist in the library.
    *
    * @param accountDerivation derivation information specified for this account.
    * @param user the user who can access the account.
    * @param poolName the name of the wallet pool the account belongs to.
    * @param walletName the name of the wallet the account belongs to.
    * @return a Future of new instance of `co.ledger.wallet.daemon.models.Account`.
    */
  def createAccount(accountDerivation: AccountDerivationView, user: User, poolName: String, walletName: String): Future[Account]

  /**
    * Method to create an account instance with extended account. The account may already exist in the library.
    *
    * @param accountDerivation derivation information specified for this account.
    * @param user the user who can access the account.
    * @param poolName the name of the wallet pool the account belongs to.
    * @param walletName the name of the wallet the account belongs to.
    * @return a Future of new instance of `co.ledger.wallet.daemon.models.Account`.
    */
  def createAccount(accountDerivation: AccountExtendedDerivationView, user: User, poolName: String, walletName: String): Future[Account]


  /**
    * Getter of accounts sequence with specified parameters.
    *
    * @param pubKey the public key of instance of `co.ledger.wallet.daemon.DefaultDaemonCache.User`.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @return a Future of a sequence of instances of `co.ledger.wallet.daemon.models.Account`.
    */
  def getAccounts(pubKey: String, poolName: String, walletName: String): Future[Seq[Account]]

  /**
    * Getter of account instance with specified parameters.
    *
    * @param accountIndex the unique index of specified instance.
    * @param pubKey the public key of instance of `co.ledger.wallet.daemon.DefaultDaemonCache.User`.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @return a Future of an Option of the instance of `co.ledger.wallet.daemon.models.Account`.
    */
  def getAccount(accountIndex: Int, pubKey: String, poolName: String, walletName: String): Future[Option[Account]]

  /**
    * Getter for fresh addresses of specified account.
    *
    * @param accountIndex the unique index of specified account.
    * @param pubKey the publick key of instance of `co.ledger.wallet.daemon.DefaultDaemonCache.User`.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @return a Future of a sequence of instances of `co.ledger.wallet.daemon.models.Account`.
    */
  def getDerivationPath(accountIndex: Int, pubKey: String, poolName: String, walletName: String): Future[String]


  /**
    * Getter for fresh addresses of specified account.
    *
    * @param accountIndex the unique index of specified account.
    * @param user the user who can access the account.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @return a Future of a sequence of instances of `co.ledger.wallet.daemon.models.Account`.
    */
  def getFreshAddresses(accountIndex: Int, user: User, poolName: String, walletName: String): Future[Seq[String]]

  /**
    * Getter of account operations batch instances with specified parameters.
    *
    * @param user the user who can access the account.
    * @param accountIndex the unique index of specified instance.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param batch the operations count that need to be queried.
    * @param fullOp the flag specifying the query result details. If greater than zero, detailed operations,
    *               including transaction information, will be returned.
    * @return a Future of `co.ledger.wallet.daemon.models.PackedOperationsView` instance.
    */
  def getAccountOperations(user: User, accountIndex: Int, poolName: String, walletName: String, batch: Int, fullOp: Int): Future[PackedOperationsView]

  /**
    * Getter for account operation instance with specified uid.
    *
    * @param user the user who can access the account.
    * @param uid the unique identifier of operation defined by core lib.
    * @param accountIndex the unique account index.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param fullOp the flag specifying the query result details. If greater than zero, detailed operations,
    *               including transaction information, will be returned.
    * @return a Future of optional `co.ledger.wallet.daemon.models.Operation` instance.
    */
  def getAccountOperation(user: User, uid: String, accountIndex: Int, poolName: String, walletName: String, fullOp: Int): Future[Option[OperationView]]

  /**
    * Getter of account operations batch instances with specified parameters.
    *
    * @param user the user who can access the account.
    * @param accountIndex the unique index of specified instance.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param next the UUID indicating the offset of returning batch.
    * @param fullOp the flag specifying the query result details. If greater than zero, detailed operations,
    *               including transaction information, will be returned.
    * @return a Future of `co.ledger.wallet.daemon.models.PackedOperationView` instance.
    */
  def getNextBatchAccountOperations(user: User, accountIndex: Int, poolName: String, walletName: String, next: UUID, fullOp: Int): Future[PackedOperationsView]

  /**
    * Getter of account operations batch instances with specified parameters.
    *
    * @param user the user who can access the account.
    * @param accountIndex the unique index of specified account instance.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param previous the UUID indicating the offset of returning batch. The batch should be already requested
    *                 by another transaction.
    * @param fullOp the flag specifying the query result details. If greater than zero, detailed operations,
    *               including transaction information, will be returned.
    * @return a Future of `co.ledger.wallet.daemon.models.PackedOperationView` instance.
    */
  def getPreviousBatchAccountOperations(
                                         user: User,
                                         accountIndex: Int,
                                         poolName: String,
                                         walletName: String,
                                         previous: UUID,
                                         fullOp: Int): Future[PackedOperationsView]


  /**
    * Getter of information for next account creation with extended keys.
    *
    * @param pubKey the public key of user.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param accountIndex the unique index of the account. If `None`, a default index will be created. If the
    *                     specified index already exists in core library, an error will occur. `None` is recommended.
    * @return a Future of `co.ledger.wallet.daemon.models.Derivation` instance.
    */
  def getNextExtendedAccountCreationInfo(pubKey: String, poolName: String, walletName: String, accountIndex: Option[Int]): Future[ExtendedDerivation]

  /**
    * Getter of information for next account creation.
    *
    * @param pubKey the public key of user.
    * @param poolName the name of wallet pool the account belongs to.
    * @param walletName the name of wallet the account belongs to.
    * @param accountIndex the unique index of the account. If `None`, a default index will be created. If the
    *                     specified index already exists in core library, an error will occur. `None` is recommended.
    * @return a Future of `co.ledger.wallet.daemon.models.Derivation` instance.
    */
  def getNextAccountCreationInfo(pubKey: String, poolName: String, walletName: String, accountIndex: Option[Int]): Future[Derivation]

  // ************** currency ************
  /**
    * Getter of `co.ledger.wallet.daemon.models.Currency` instance.
    *
    * @param currencyName the name of specified currency. Name is predefined by core library and is the
    *                     identifier of currencies.
    * @param poolName the name of wallet pool for this currency.
    * @param pubKey the public key of user.
    * @return a Future of `co.ledger.wallet.daemon.models.Currency` Option.
    */
  def getCurrency(currencyName: String, poolName: String, pubKey: String): Future[Option[Currency]]

  /**
    * Getter of `co.ledger.wallet.daemon.models.Currency` instances sequence.
    *
    * @param poolName the name of wallet pool of this currency.
    * @param pubKey the public key of user.
    * @return a Future of sequence of `co.ledger.wallet.daemon.models.Currency` instances.
    */
  def getCurrencies(poolName: String, pubKey: String): Future[Seq[Currency]]

  // ************** wallet *************
  /**
    * Method to create a wallet instance.
    *
    * @param walletName the name of this wallet.
    * @param currencyName the name of currency of this wallet.
    * @param poolName the name of wallet pool contains the wallet.
    * @param user the user who can access the wallet.
    * @return a Future of `co.ledger.wallet.daemon.models.Wallet` instance created.
    */
  def createWallet(walletName: String, currencyName: String, poolName: String, user: User): Future[Wallet]

  /**
    * Getter of sequence of `co.ledger.wallet.daemon.models.Wallet` instances.
    *
    * @param offset the offset of the returned wallet sequence.
    * @param batch the batch size of the returned wallet sequence.
    * @param poolName the name of wallet pool the wallets belong to.
    * @param pubKey the public key of the user.
    * @return a Future of a tuple containing the total wallets count and required sequence of wallets.
    */
  def getWallets(offset: Int, batch: Int, poolName: String, pubKey: String): Future[(Int, Seq[Wallet])]

  /**
    * Getter of instance of `co.ledger.wallet.daemon.models.Wallet`.
    *
    * @param walletName the name of specified wallet.
    * @param poolName the name of the pool the wallet belongs to.
    * @param pubKey the public key of the user.
    * @return a Future of `co.ledger.wallet.daemon.models.Wallet` instance Option.
    */
  def getWallet(walletName: String, poolName: String, pubKey: String): Future[Option[Wallet]]

  // ************** wallet pool *************
  /**
    * Method to create an instance of wallet pool.
    *
    * @param user the user who can access the wallet pool.
    * @param poolName the name of this created pool.
    * @param configuration the extra configuration can be set to the pool.
    * @return a Future of `co.ledger.wallet.daemon.models.Pool` instance.
    */
  def createWalletPool(user: User, poolName: String, configuration: String): Future[Pool]

  /**
    * Getter of instance of `co.ledger.wallet.daemon.models.Wallet`.
    *
    * @param pubKey the public key of user who can access the pool.
    * @param poolName the name of wallet pool.
    * @return a Future of `co.ledger.wallet.daemon.models.Pool` instance Option.
    */
  def getWalletPool(pubKey: String, poolName: String): Future[Option[Pool]]

  /**
    * Getter of sequence of instances of `co.ledger.wallet.daemon.models.Pool`.
    *
    * @param pubKey the public key of user who can access the pool.
    * @return a Future of sequence of `co.ledger.wallet.daemon.models.Pool` instances.
    */
  def getWalletPools(pubKey: String): Future[Seq[Pool]]

  /**
    * Method to delete wallet pool instance. This operation will delete the pool record from daemon database and
    * dereference the pool from core library.
    *
    * @param user the user who can operate the pool.
    * @param poolName the name of the wallet pool needs to be deleted.
    * @return a Future of Unit.
    */
  def deleteWalletPool(user: User, poolName: String): Future[Unit]

  /**
    * Method to synchronize account operations from public resources. The method may take a while
    * to finish.
    *
    * @return a Future of sequence of result of synchronization.
    */
  def syncOperations(): Future[Seq[SynchronizationResult]]

  /**
    * Method to synchronize account operations from public resources. The method may take a while
    * to finish. This method only synchronize a single pool.
    *
    * @return a Future of sequence of result of synchronization.
    */
  def syncOperations(pubKey: String, poolName: String): Future[Seq[SynchronizationResult]]

  /**
    * Method to synchronize account operations from public resources. The method may take a while
    * to finish. This method onlly synchronize a single account.
    *
    * @return a Future of sequence of result of synchronization.
    */
  def syncOperations(pubKey: String, poolName: String, walletName: String, accountIndex: Int): Future[Seq[SynchronizationResult]]

  //**************** user ***************
  /**
    * Getter of user instance.
    *
    * @param pubKey the public key related to this user.
    * @return a Future of User instance Option.
    */
  def getUser(pubKey: String): Future[Option[User]]

  /**
    * Method to create a user instance.
    *
    * @param pubKey public key of this user.
    * @param permissions the permissions level of this user.
    * @return a Future of unique id of created user.
    */
  def createUser(pubKey: String, permissions: Int): Future[Long]

}
