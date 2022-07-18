package shop.tryit.web.notice;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import shop.tryit.domain.member.Member;
import shop.tryit.domain.member.MemberService;
import shop.tryit.domain.notice.Notice;
import shop.tryit.domain.notice.NoticeService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final MemberService memberService;

    @GetMapping("/new")
    public String saveForm(@ModelAttribute NoticeSaveFormDto noticeSaveFormDto) {
        return "notices/register";
    }

    @PostMapping("/new")
    public String save(@Valid @ModelAttribute NoticeSaveFormDto noticeSaveFormDto,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal User user
    ) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult = '{}'", bindingResult);
            return "notices/register";
        }
        String userEmail = user.getUsername();
        Member member = memberService.findMember(userEmail);
        Notice notice = NoticeAdapter.toEntity(noticeSaveFormDto, member);
        Long savedId = noticeService.save(notice);
        return String.format("redirect:/notices/%s", savedId);
    }

    @GetMapping("/{noticeId}")
    public String findOne(@PathVariable Long noticeId,
                          Model model
    ) {
        Notice notice = noticeService.findById(noticeId);
        String memberEmail = noticeService.findMemberEmailById(noticeId);
        NoticeViewFormDto noticeViewFormDto = NoticeAdapter.toViewForm(notice, memberEmail);
        model.addAttribute("noticeViewFormDto", noticeViewFormDto);
        return "notices/detail";
    }

    @GetMapping("/{noticeId}/update")
    public String updateForm(@PathVariable Long noticeId,
                             Model model
    ) {
        Notice notice = noticeService.findById(noticeId);
        String memberEmail = noticeService.findMemberEmailById(noticeId);
        NoticeUpdateFormDto noticeUpdateFormDto = NoticeAdapter.toUpdateForm(notice, memberEmail);
        model.addAttribute("noticeUpdateFormDto", noticeUpdateFormDto);
        return "notices/update";
    }

    @PostMapping("/{noticeId}/update")
    public String update(@PathVariable Long noticeId,
                         @ModelAttribute NoticeUpdateFormDto noticeUpdateFormDto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal User user
    ) {
        if (bindingResult.hasErrors()) {
            log.info("bindingResult = '{}'", bindingResult);
            return "notices/register";
        }

        String userEmail = user.getUsername();
        Member member = memberService.findMember(userEmail);

        Notice newNotice = NoticeAdapter.toEntity(noticeUpdateFormDto, member);
        noticeService.update(noticeId, newNotice);

        return String.format("redirect:/notices/%s", noticeId);
    }

    @PostMapping("/{noticeId}/delete")
    public String delete(@PathVariable Long noticeId) {
        noticeService.delete(noticeId);
        return "redirect:/notices";
    }

}