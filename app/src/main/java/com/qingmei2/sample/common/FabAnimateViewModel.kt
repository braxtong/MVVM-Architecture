package com.qingmei2.sample.common

import androidx.recyclerview.widget.RecyclerView
import arrow.core.left
import arrow.core.right
import com.qingmei2.rhine.util.RxSchedulers
import com.qingmei2.sample.http.Errors
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.zipWith

val listScrollChangeStateProcessor: Function<Int, Observable<Boolean>>
    get() = Function { currentState ->
        Observable.just(currentState)
                .map { it == RecyclerView.SCROLL_STATE_IDLE }
                .compose { upstream ->
                    upstream.zipWith(upstream.startWith(true)) { last, current ->
                        when (last == current) {
                            true -> Errors.EmptyInputError.left()
                            false -> current.right()
                        }
                    }
                }
                .flatMap { changed ->
                    changed.fold({
                        Observable.empty<Boolean>()
                    }, {
                        Observable.just(it)
                    })
                }
                .observeOn(RxSchedulers.ui)
    }