package me.anon.grow3.ui.crud.fragment

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentCrudDiaryCropsBinding
import me.anon.grow3.databinding.StubCrudCropBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.CropActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
import java.util.*
import javax.inject.Inject

class DiaryCropsFragment : BaseFragment(FragmentCrudDiaryCropsBinding::class.java)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by lazy { binding<FragmentCrudDiaryCropsBinding>() }

	override fun bindUi()
	{
		viewBindings.addCrop.onClick {
			// reveal crop edit fragment dialog
			navigateForResult<CropActivity> {
				putExtra(CropActivity.EXTRA_DIARY_ID, viewModel.diary.value?.asSuccess()?.id)
			}
		}
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			if (!diary.isSuccess) return@observe

			val diary = diary.asSuccess()
			viewBindings.cropsContainer.removeAllViews()
			diary.crops.forEach { crop ->
				val view = viewBindings.cropsContainer.inflate<View>(R.layout.stub_crud_crop)
				val cropBindings = StubCrudCropBinding.bind(view)
				cropBindings.cropName.text = crop.name

				cropBindings.cropGenetics.text = crop.genetics
				cropBindings.cropGenetics.isVisible = !crop.genetics.isNullOrBlank()

				cropBindings.duplicate.onClick {
					viewModel.addCrop(crop.copy(id = UUID.randomUUID().toString()))
				}

				view.onClick {
					// reveal crop edit fragment dialog
					navigateForResult<CropActivity> {
						putExtra(CropActivity.EXTRA_DIARY_ID, diary.id)
						putExtra(CropActivity.EXTRA_CROP_ID, crop.id)
					}
				}

//				 Broken for now
//				BadgeUtils.attachBadgeDrawable(BadgeDrawable.create(view.context).apply {
//					this.number = crop.numberOfPlants
//					this.backgroundColor = R.attr.colorSecondary.resColor(view.context)
//					this.badgeGravity = BadgeDrawable.TOP_END
//				}, view.crop_image, null))

				viewBindings.cropsContainer += view
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		when (requestCode)
		{
			code<CropActivity>() -> {
				viewModel.refresh()
			}
		}

		super.onActivityResult(requestCode, resultCode, data)
	}
}